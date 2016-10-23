/**
 * Copyright 2016 Fabio Ticconi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.fabioticconi.roguelite.utils;

/**
 * Functional interface that takes two integers and returns a type.
 *
 * @author Fabio Ticconi
 */
@FunctionalInterface
public interface BiIntFunction<R>
{

    /**
     * Applies this function to the given arguments.
     *
     * @param value1
     *            first function argument
     * @param value2
     *            second function argument
     * @return the function result
     */
    R apply(int value1, int value2);
}
