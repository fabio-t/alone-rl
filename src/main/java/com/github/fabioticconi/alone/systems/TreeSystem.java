/*
 * Copyright (C) 2017 Fabio Ticconi
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
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.constants.WeaponType;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.map.SingleGrid;
import com.github.fabioticconi.alone.messages.CannotMsg;
import com.github.fabioticconi.alone.messages.CutMsg;
import com.github.fabioticconi.alone.utils.Util;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.EnumSet;

/**
 * Author: Fabio Ticconi
 * Date: 07/10/17
 */
public class TreeSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(TreeSystem.class);

    ComponentMapper<Tree>      mTree;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Strength>  mStrength;
    ComponentMapper<Position>  mPosition;
    ComponentMapper<Name>      mName;

    StaminaSystem sStamina;
    ItemSystem    sItem;
    MessageSystem msg;

    @Wire
    SingleGrid obstacles;

    @Wire
    MultipleGrid items;

    public CutAction cut(final int entityId, final int treeId)
    {
        final CutAction c = new CutAction();

        c.actorId = entityId;

        c.targets.add(treeId);

        return c;
    }

    public int makeTree(final int x, final int y)
    {
        final int id = world.create();

        final EntityEdit edit = world.edit(id);
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set('T', Color.GREEN.darker(), true);
        edit.create(LightBlocker.class);
        edit.create(Tree.class);
        edit.add(new Name("A tree"));

        return id;
    }

    public int makeTrunk(final int x, final int y)
    {
        final int id = world.create();

        final EntityEdit edit = world.edit(id);
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set('-', Util.BROWN.brighter());
        edit.add(new Name("A tree trunk"));

        return id;
    }

    public int makeBranch(final int x, final int y)
    {
        final int id = world.create();

        final EntityEdit edit = world.edit(id);
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set('/', Util.BROWN.brighter());
        edit.create(Weapon.class).set(WeaponType.BLUNT, 1);
        edit.add(new Name("A branch"));

        return id;
    }

    public class CutAction extends ActionContext
    {
        @Override
        public boolean tryAction()
        {
            if (targets.size() != 1)
                return false;

            final int treeId = targets.get(0);

            if (!mTree.has(treeId))
                return false;

            final int axeId = sItem.getWeapon(actorId, EnumSet.of(WeaponType.SLASH), false);

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

            final Position p = mPosition.get(treeId);

            // from a tree we get a trunk and two branches
            obstacles.del(p.x, p.y);
            world.delete(treeId);

            items.add(makeTrunk(p.x, p.y), p.x, p.y);
            items.add(makeBranch(p.x, p.y), p.x, p.y);
            items.add(makeBranch(p.x, p.y), p.x, p.y);

            // consume a fixed amount of stamina
            sStamina.consume(actorId, cost);

            msg.send(actorId, treeId, new CutMsg());
        }
    }
}
