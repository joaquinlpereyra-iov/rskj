/*
 * This file is part of RskJ
 * Copyright (C) 2019 RSK Labs Ltd.
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

package co.rsk.util;

import java.util.function.Function;

public interface Either<L,R> {

    static <L,R> Either<L,R> left(L value) {
        return new Left<>(value);
    }

    static <L,R> Either<L,R> right(R value) {
        return new Right<>(value);
    }

    <T> T either(Function<? super L, ? extends T> lFunc, Function<? super R, ? extends T> rFunc);

    <T> Either<T,R> mapLeft(Function<? super L, ? extends T> lFunc);

    <T> Either<L,T> mapRight(Function<? super R, ? extends T> rFunc);

    final class Left<L, R> implements Either<L, R> {

        private final L value;

        private Left(L value) {
            this.value = value;
        }

        public <T> T either(Function<? super L, ? extends T> lFunc, Function<? super R, ? extends T> rFunc) {
            return lFunc.apply(this.value);
        }

        public <T> Either<T,R> mapLeft(Function<? super L, ? extends T> lFunc) {
            return Either.left(lFunc.apply(this.value));
        }

        public <T> Either<L,T> mapRight(Function<? super R, ? extends T> rFunc) {
            return Either.left(this.value);
        }
    }

    final class Right<L, R> implements Either<L, R> {

        private final R value;

        private Right(R value) {
            this.value = value;
        }

        public <T> T either(Function<? super L, ? extends T> lFunc, Function<? super R, ? extends T> rFunc) {
            return rFunc.apply(this.value);
        }

        public <T> Either<T,R> mapLeft(Function<? super L, ? extends T> lFunc) {
            return Either.right(this.value);

        }

        public <T> Either<L,T> mapRight(Function<? super R, ? extends T> rFunc) {
            return Either.right(rFunc.apply(this.value));
        }
    }
}
