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

package co.rsk.net;

import co.rsk.crypto.Keccak256;
import org.ethereum.core.Block;
import org.ethereum.crypto.HashUtil;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class BlockCacheTest {

    private static final Keccak256 HASH_1 = new Keccak256(HashUtil.sha256(new byte[]{1}));
    private static final Keccak256 HASH_2 = new Keccak256(HashUtil.sha256(new byte[]{2}));
    private static final Keccak256 HASH_3 = new Keccak256(HashUtil.sha256(new byte[]{3}));
    private static final Keccak256 HASH_4 = new Keccak256(HashUtil.sha256(new byte[]{4}));
    private static final Keccak256 HASH_5 = new Keccak256(HashUtil.sha256(new byte[]{5}));

    @Test
    public void getUnknownBlockAsNull() {
        BlockStoreCache store = getSubject();
        assertFalse(store.getBlockByHash(HASH_1).isPresent());
    }

    @Test
    public void putAndGetValue() {
        BlockStoreCache store = getSubject();
        Block wrappedValue = mock(Block.class);
        store.addBlock(HASH_1, wrappedValue);

        assertTrue(store.getBlockByHash(HASH_1).isPresent());
        assertThat(store.getBlockByHash(HASH_1).get(), is(wrappedValue));
    }

    @Test
    public void putMoreThanSizeAndCheckCleanup() {
        BlockStoreCache store = getSubject();
        store.addBlock(HASH_1, mock(Block.class));
        store.addBlock(HASH_2, mock(Block.class));
        store.addBlock(HASH_3, mock(Block.class));
        store.addBlock(HASH_4, mock(Block.class));
        store.addBlock(HASH_5, mock(Block.class));

        assertFalse(store.getBlockByHash(HASH_1).isPresent());
        assertTrue(store.getBlockByHash(HASH_2).isPresent());
        assertTrue(store.getBlockByHash(HASH_3).isPresent());
        assertTrue(store.getBlockByHash(HASH_4).isPresent());
        assertTrue(store.getBlockByHash(HASH_5).isPresent());
    }

    @Test
    public void repeatingValueAtEndPreventsCleanup() {
        BlockStoreCache store = getSubject();
        store.addBlock(HASH_1, mock(Block.class));
        store.addBlock(HASH_2, mock(Block.class));
        store.addBlock(HASH_3, mock(Block.class));
        store.addBlock(HASH_4, mock(Block.class));
        store.addBlock(HASH_5, mock(Block.class));
        store.addBlock(HASH_1, mock(Block.class));
        store.addBlock(HASH_5, mock(Block.class));

        assertTrue(store.getBlockByHash(HASH_1).isPresent());
        assertFalse(store.getBlockByHash(HASH_2).isPresent());
        assertTrue(store.getBlockByHash(HASH_3).isPresent());
        assertTrue(store.getBlockByHash(HASH_4).isPresent());
        assertTrue(store.getBlockByHash(HASH_5).isPresent());
    }

    @Test
    public void addAndRemoveBlock() {
        BlockStoreCache store = getSubject();
        Block wrappedValue = mock(Block.class);
        store.addBlock(HASH_1, wrappedValue);
        store.removeValue(HASH_1);

        assertFalse(store.getBlockByHash(HASH_1).isPresent());
    }

    private BlockStoreCache getSubject() {
        return new BlockStoreCache(4);
    }
}