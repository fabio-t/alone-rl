/*
 * Copyright (C) 2015-2017 Fabio Ticconi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.fabioticconi.alone.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.components.attributes.Agility;
import com.github.fabioticconi.alone.components.attributes.Skin;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.constants.BodyPart;
import com.github.fabioticconi.alone.constants.DamageType;
import com.github.fabioticconi.alone.messages.DamageMsg;
import com.github.fabioticconi.alone.messages.KillMsg;
import com.github.fabioticconi.alone.messages.MissMsg;
import com.github.fabioticconi.alone.utils.Coords;
import com.github.fabioticconi.alone.utils.Util;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Random;

/**
 * Author: Fabio Ticconi
 * Date: 28/09/17
 */
public class AttackSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(AttackSystem.class);

    ComponentMapper<Strength> mStrength;
    ComponentMapper<Agility>  mAgility;
    ComponentMapper<Health>   mHealth;
    ComponentMapper<Skin>     mSkin;
    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Dead>     mDead;
    ComponentMapper<Position> mPos;
    ComponentMapper<Weapon>   mWeapon;
    ComponentMapper<Armour>   mArmour;

    @Wire
    Random r;

    StaminaSystem sStamina;
    ItemSystem    sItem;
    MessageSystem msg;

    public AttackAction attack(final int entityId, final int targetId)
    {
        final AttackAction a = new AttackAction();

        a.actorId = entityId;
        a.targets.add(targetId);

        return a;
    }

    public class AttackAction extends ActionContext
    {
        @Override
        public boolean tryAction()
        {
            if (targets.size() != 1)
                return false;

            final int targetId = targets.get(0);

            final Position p  = mPos.get(actorId);
            final Position p2 = mPos.get(targetId);

            if (Coords.distanceChebyshev(p.x, p.y, p2.x, p2.y) != 1)
            {
                // can't strike if the target has moved away

                return false;
            }

            // FIXME check the target is close by?

            final float speed = mSpeed.get(actorId).value;

            // FIXME maybe dependent on strength and/or weight of weapon?
            cost = 0.5f;

            delay = speed * cost;

            return true;
        }

        @Override
        public void doAction()
        {
            if (targets.size() != 1)
                return;

            final int targetId = targets.get(0);

            final Position p  = mPos.get(actorId);
            final Position p2 = mPos.get(targetId);

            if (Coords.distanceChebyshev(p.x, p.y, p2.x, p2.y) != 1)
            {
                // can't strike if the target has moved away
                return;
            }

            final Strength cStrength = mStrength.get(actorId);
            final Agility  cAgility  = mAgility.get(actorId);

            final Agility tAgility = mAgility.get(targetId);
            final Health  tHealth  = mHealth.get(targetId);
            final Skin    tSkin    = mSkin.get(targetId);

            // whether it hits or not, both attacker and defender get a penalty to their stamina
            // (fixed, small cost for the defender)
            sStamina.consume(actorId, cost);
            sStamina.consume(targetId, 0.25f);

            final float toHit = Util.clamp((cAgility.value - tAgility.value + 4) / 8f, 0.05f, 0.95f);

            if (r.nextFloat() < toHit)
            {
                float      damage  = cStrength.value + 2f;
                float      armour  = tSkin.value;
                DamageType dmgType = DamageType.NATURAL;

                // the weapon damage is added to the strength-based one, so that creatures
                // wielding weapons can overcome stronger, unharmed creatures
                final int weaponId = sItem.getWeapon(actorId, EnumSet.allOf(DamageType.class), true);
                if (weaponId >= 0)
                {
                    final Weapon w = mWeapon.get(weaponId);
                    damage += w.damage;
                    dmgType = w.damageType;
                }
                // or maybe we ARE a weapon (eg, if thrown or shot)
                else if (mWeapon.has(actorId))
                {
                    final Weapon w = mWeapon.get(actorId);
                    damage += w.damage;
                    dmgType = w.damageType;
                }

                final int armourId = sItem.getArmour(targetId, EnumSet.of(BodyPart.BODY), true);
                if (armourId >= 0)
                {
                    final Armour a = mArmour.get(armourId);
                    armour += a.defences.get(dmgType);
                }

                // the armour absorbs some or all the damage
                damage -= armour;

                // every successful hit removes at least one hp, always
                tHealth.value -= Math.max(damage, 1f);

                msg.send(actorId, targetId, new DamageMsg(damage, tHealth.value));

                if (tHealth.value <= 0)
                {
                    msg.send(actorId, targetId, new KillMsg());

                    mDead.create(targetId);
                }
            }
            else
            {
                msg.send(actorId, targetId, new MissMsg());
            }
        }
    }
}
