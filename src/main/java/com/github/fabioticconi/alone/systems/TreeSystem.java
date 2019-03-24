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
import com.github.fabioticconi.alone.components.Cuttable;
import com.github.fabioticconi.alone.components.Position;
import com.github.fabioticconi.alone.components.Speed;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.constants.DamageType;
import com.github.fabioticconi.alone.messages.CannotMsg;
import com.github.fabioticconi.alone.messages.CutMsg;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Random;

/**
 * Author: Fabio Ticconi
 * Date: 07/10/17
 */
public class TreeSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(TreeSystem.class);

    ComponentMapper<Cuttable> mCuttable;
    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Strength> mStrength;
    ComponentMapper<Position> mPosition;

    StaminaSystem sStamina;
    ItemSystem    sItem;
    MessageSystem msg;
    MapSystem     map;

    @Wire
    Random r;

    public CutAction cut(final int entityId, final int treeId)
    {
        final CutAction c = new CutAction();

        c.actorId = entityId;

        c.targets.add(treeId);

        return c;
    }

    public class CutAction extends ActionContext
    {
        @Override
        public boolean tryAction()
        {
            if (targets.size() != 1)
                return false;

            final int treeId = targets.get(0);

            if (!mCuttable.has(treeId))
                return false;

            final int axeId = sItem.getWeapon(actorId, EnumSet.of(DamageType.SLASH), false);

            if (axeId < 0)
            {
                msg.send(actorId, treeId, new CannotMsg("cut", "without a slashing weapon"));

                return false;
            }

            // FIXME further adjust delay and cost using the axe power (in this way, cutting down a tree
            // with a cutting knife will be possible but long and hard, while using a strong axe will be
            // quick and easy
            delay = mSpeed.get(actorId).value;
            cost = delay / (mStrength.get(actorId).value + 3f);

            targets.add(axeId);

            return true;
        }

        public void doAction()
        {
            if (targets.size() != 2)
                return;

            final int treeId = targets.get(0);

            msg.send(actorId, treeId, new CutMsg());

            final Position p = mPosition.get(treeId);

            // from a tree we get a trunk and two branches
            map.obstacles.del(p.x, p.y);
            world.delete(treeId);

            sItem.makeItem("trunk", p.x, p.y);

            if (r.nextBoolean())
                sItem.makeItem("branch", p.x, p.y);

            if (r.nextBoolean())
                sItem.makeItem("vine", p.x, p.y);

            // consume a fixed amount of stamina
            sStamina.consume(actorId, cost);
        }
    }
}
