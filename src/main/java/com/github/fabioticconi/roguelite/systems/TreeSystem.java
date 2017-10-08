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

package com.github.fabioticconi.roguelite.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.artemis.systems.DelayedIteratingSystem;
import com.github.fabioticconi.roguelite.components.*;
import com.github.fabioticconi.roguelite.components.actions.CutAction;
import com.github.fabioticconi.roguelite.map.MultipleGrid;
import com.github.fabioticconi.roguelite.map.SingleGrid;
import com.github.fabioticconi.roguelite.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Author: Fabio Ticconi
 * Date: 07/10/17
 */
public class TreeSystem extends DelayedIteratingSystem
{
    static final Logger log = LoggerFactory.getLogger(TreeSystem.class);

    ComponentMapper<Tree>      mTree;
    ComponentMapper<Inventory> mInventory;
    ComponentMapper<Weapon>    mWeapon;
    ComponentMapper<CutAction> mCut;
    ComponentMapper<Speed>     mSpeed;
    ComponentMapper<Position>  mPosition;

    StaminaSystem sStamina;

    @Wire
    SingleGrid obstacles;

    @Wire
    MultipleGrid items;

    public TreeSystem()
    {
        super(Aspect.all(CutAction.class));
    }

    @Override
    protected float getRemainingDelay(final int entityId)
    {
        return mCut.get(entityId).cooldown;
    }

    @Override
    protected void processDelta(final int entityId, final float accumulatedDelta)
    {
        mCut.get(entityId).cooldown -= accumulatedDelta;
    }

    @Override
    protected void processExpired(final int entityId)
    {
        final CutAction cut = mCut.get(entityId);

        // whatever the outcome, we remove the component
        mCut.remove(entityId);

        final int targetId = cut.targetId;

        // something hijacked us and cut the tree beforehand, maybe
        if (targetId < 0 || !mTree.has(targetId))
            return;

        final Position p = mPosition.get(targetId);

        // from a tree we get a trunk and two branches
        obstacles.del(p.x, p.y);
        world.delete(targetId);

        items.add(makeTrunk(p.x, p.y), p.x, p.y);
        items.add(makeBranch(p.x, p.y), p.x, p.y);
        items.add(makeBranch(p.x, p.y), p.x, p.y);

        // consume a fixed amount of stamina
        sStamina.consume(entityId, cut.cost);
    }

    public float cut(final int entityId, final int treeId)
    {
        if (treeId < 0 || !mTree.has(treeId))
            return 0f;

        final Inventory items = mInventory.get(entityId);

        if (items == null)
            return 0f;

        final int[] data = items.items.getData();
        for (int i = 0; i < items.items.size(); i++)
        {
            final int itemId = data[i];

            if (itemId < 0)
            {
                // TODO: we could flag inventory as "dirty", and then use a system for periodic cleanup.

                continue;
            }

            final Weapon weapon = mWeapon.get(itemId);

            // need a slashing weapon to cut down the tree
            if (weapon == null || !weapon.damageType.equals(Weapon.Type.SLASH))
                continue;

            final CutAction cut = mCut.create(entityId);

            if (cut.targetId == itemId)
            {
                // we are already cutting down that tree, don't reset timer
                return cut.cooldown;
            }

            final float speed = mSpeed.get(entityId).value;

            cut.set(speed, itemId, 1.5f);

            offerDelay(speed);

            return speed;
        }

        log.info("{} cannot cut down tree {}: no suitable weapon", entityId, treeId);

        return 0f;
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
        edit.create(Sprite.class).set('_', Util.BROWN.brighter());

        return id;
    }

    public int makeBranch(final int x, final int y)
    {
        final int id = world.create();

        final EntityEdit edit = world.edit(id);
        edit.create(Position.class).set(x, y);
        edit.create(Sprite.class).set('-', Util.BROWN.brighter());
        edit.create(Weapon.class).set(Weapon.Type.BLUNT, 1, false);

        return id;
    }
}
