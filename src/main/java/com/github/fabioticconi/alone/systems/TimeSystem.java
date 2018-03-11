/*
 * Copyright (C) 2015-2018 Fabio Ticconi
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

import com.artemis.BaseSystem;

/**
 * Author: Fabio Ticconi
 * Date: 11/03/18
 */
public class TimeSystem extends BaseSystem
{
    private float currentTimeInSeconds;
    private float realSecondsPerHour;

    private int inGameHour;

    public TimeSystem(final int inGameHour, final float realSecondsPerHour)
    {
        // the hour the game starts at
        this.inGameHour = inGameHour;

        // how many simulation-seconds we need to make one game-hour?
        this.realSecondsPerHour = realSecondsPerHour;
    }

    @Override
    protected void processSystem()
    {
        final float delta = getWorld().delta;

        currentTimeInSeconds += delta;

        if (currentTimeInSeconds > realSecondsPerHour)
        {
            currentTimeInSeconds -= realSecondsPerHour;
            inGameHour++;

            if (inGameHour > 23)
                inGameHour = 0;

            System.out.println(inGameHour);
        }
    }

    public int getCurrentHour()
    {
        return inGameHour;
    }

    public int getHoursFromMidnight()
    {
        if (inGameHour > 16)
            return 24 - inGameHour;
        else
            return inGameHour;
    }
}
