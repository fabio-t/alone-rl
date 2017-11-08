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
import com.github.fabioticconi.alone.components.*;
import com.github.fabioticconi.alone.components.actions.ActionContext;
import com.github.fabioticconi.alone.components.attributes.Strength;
import com.github.fabioticconi.alone.constants.Cell;
import com.github.fabioticconi.alone.constants.WeaponType;
import com.github.fabioticconi.alone.messages.CannotMsg;
import com.github.fabioticconi.alone.messages.CutMsg;
import com.github.fabioticconi.alone.utils.Util;
import net.mostlyoriginal.api.system.core.PassiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rlforj.math.Point;

import java.awt.*;
import java.util.EnumSet;

/**
 * Author: Fabio Ticconi
 * Date: 07/10/17
 */
public class TreeSystem extends PassiveSystem
{
    static final Logger log = LoggerFactory.getLogger(TreeSystem.class);

    ComponentMapper<Tree>     mTree;
    ComponentMapper<Speed>    mSpeed;
    ComponentMapper<Strength> mStrength;
    ComponentMapper<Position> mPosition;
    ComponentMapper<Name>     mName;

    StaminaSystem sStamina;
    ItemSystem    sItem;
    MessageSystem msg;
    MapSystem     map;

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
        edit.add(new Name("A mature tree", "tree"));

        map.obstacles.set(id, x, y);

        return id;
    }

    public int makeTrunk(final Point p)
    {
        return makeTrunk(p.x, p.y);
    }

    public int makeTrunk(final int x, final int y)
    {
        final int id = world.create();

        final EntityEdit edit = world.edit(id);
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set((char)22, Util.BROWN.brighter());
        edit.add(new Name("A fallen tree", "trunk"));

        map.items.set(id, x, y);

        return id;
    }

    public int makeBranch(final Point p)
    {
        return makeBranch(p.x, p.y);
    }

    public int makeBranch(final int x, final int y)
    {
        final int id = world.create();

        final EntityEdit edit = world.edit(id);
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set('/', Util.BROWN.brighter());
        edit.create(Weapon.class).set(WeaponType.BLUNT, 1);
        edit.create(Wearable.class);
        edit.add(new Name("A sturdy branch", "branch"));

        map.items.set(id, x, y);

        return id;
    }

    public int makeVine(final Point p)
    {
        return makeVine(p.x, p.y);
    }

    public int makeVine(final int x, final int y)
    {
        final int id = world.create();

        final EntityEdit edit = world.edit(id);
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set((char)239, Cell.GRASS.col);
        edit.add(new Name("A thin, flexible branch", "vine"));

        map.items.set(id, x, y);

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

            msg.send(actorId, treeId, new CutMsg());

            final Position p = mPosition.get(treeId);

            // from a tree we get a trunk and two branches
            map.obstacles.del(p.x, p.y);
            world.delete(treeId);

            makeTrunk(map.getFirstTotallyFree(p.x, p.y, -1));
            makeBranch(map.getFirstTotallyFree(p.x, p.y, -1));
            makeVine(map.getFirstTotallyFree(p.x, p.y, -1));

            // consume a fixed amount of stamina
            sStamina.consume(actorId, cost);
        }
    }
}
