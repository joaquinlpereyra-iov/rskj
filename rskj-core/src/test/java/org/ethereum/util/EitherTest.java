/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 * (derived from ethereumJ library, Copyright (c) 2016 <ether.camp>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.ethereum.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import co.rsk.util.Either;
import java.math.BigInteger;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EitherTest {

    @Test
    public void leftConstructor() {
        String value = "Constructing";
        Either<String, Long> either = Either.left(value);

        assertEquals(value, either.either(l -> l, r -> r));
    }

    @Test
    public void rightConstructor() {
        Long value = 50L;
        Either<String, Long> either = Either.right(value);

        assertEquals(value, either.either(l -> l, r -> r));
    }

    @Test
    public void mapLeft() {
        Long value = 50L;
        String valueMap = "50";

        Either<Long, String> either = Either.left(value);

        Either<String, String> result = either.mapLeft(v -> valueMap);

        assertEquals(valueMap, result.either(l -> l, r -> r));
    }

    @Test
    public void mapRight() {
        Long value = 50L;
        String valueMap = "50";

        Either<String, Long> either = Either.right(value);

        Either<String, String> result = either.mapRight(v -> valueMap);

        assertEquals(valueMap, result.either(l -> l, r -> r));
    }
}
