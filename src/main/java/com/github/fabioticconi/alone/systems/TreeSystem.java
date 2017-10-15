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
import com.artemis.annotations.EntityId;
import com.artemis.annotations.Wire;
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.map.MultipleGrid;
import com.github.fabioticconi.alone.map.SingleGrid;
import com.github.fabioticconi.alone.utils.Util;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

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

    StaminaSystem sStamina;
    ItemSystem    sItem;

    @Wire
    SingleGrid obstacles;

    @Wire
    MultipleGrid items;

    public CutAction cut(final int entityId, final int treeId)
    {
        final CutAction c = new CutAction();

        c.actorId = entityId;
        c.treeId = treeId;

        return c;
    }

    public int makeTree(final int x, final int y)
    {
        final int id = world.create();

        final EntityEdit edit = world.edit(id);
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set('T', Color.GREEN.brighter(), true);
        edit.create(Obstacle.class);
        edit.create(Tree.class);

        return id;
    }

    public int makeTrunk(final int x, final int y)
    {
        final int id = world.create();

        final EntityEdit edit = world.edit(id);
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set('-', Util.BROWN.brighter());

        return id;
    }

    public int makeBranch(final int x, final int y)
    {
        final int id = world.create();

        final EntityEdit edit = world.edit(id);
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set('/', Util.BROWN.brighter());
        edit.create(Weapon.class).set(Weapon.Type.BLUNT, 1, false);

        return id;
    }

    public class CutAction extends ActionContext
    {
        @EntityId
        public int treeId = -1;
        @EntityId
        public int axeId  = -1;

        @Override
        public boolean tryAction()
        {
            if (treeId < 0 || !mTree.has(treeId))
                return false;

            axeId = sItem.getWeapon(actorId, Weapon.Type.SLASH);

            if (axeId < 0)
            {
                log.info("{} cannot cut down tree {}: no suitable weapon", actorId, treeId);

                return false;
            }

            // FIXME further adjust delay and cost using the axe power (in this way, cutting down a tree
            // with a cutting knife will be possible but long and hard, while using a strong axe will be
            // quick and easy
            delay = mSpeed.get(actorId).value;
            cost = delay / (mStrength.get(actorId).value + 3f);

            return true;
        }

        public void doAction()
        {
            // something hijacked us and cut the tree beforehand, maybe
            if (treeId < 0 || !mTree.has(treeId))
                return;

            final Position p = mPosition.get(treeId);

            // from a tree we get a trunk and two branches
            obstacles.del(p.x, p.y);
            world.delete(treeId);

            items.add(makeTrunk(p.x, p.y), p.x, p.y);
            items.add(makeBranch(p.x, p.y), p.x, p.y);
            items.add(makeBranch(p.x, p.y), p.x, p.y);

            // consume a fixed amount of stamina
            sStamina.consume(actorId, cost);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (!super.equals(o))
                return false;

            final CutAction a = (CutAction) o;

            return treeId == a.treeId && axeId == a.axeId;
        }
    }
}
