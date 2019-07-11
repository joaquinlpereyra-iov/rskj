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

package org.ethereum.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.rsk.core.BlockDifficulty;
import co.rsk.crypto.Keccak256;
import co.rsk.net.BlockStoreCache;
import co.rsk.remasc.Sibling;
import co.rsk.util.MaxSizeHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.IndexedBlockStore.BlockInfo;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.DB;


public class IndexedBlockStoreTest {

    private BlockStoreEncoder blockStoreEncoder;
    private Map<Long, List<BlockInfo>> indexMap;
    private KeyValueDataSource keyValueDataSource;
    private DB indexDB;
    private IndexedBlockStore target;
    private BlockStoreCache blockStoreCache;
    private MaxSizeHashMap<Keccak256, Map<Long, List<Sibling>>> remascCache;

    @Before
    public void setup() {
        blockStoreEncoder = mock(BlockStoreEncoder.class);
        indexMap = mock(Map.class);
        keyValueDataSource = mock(KeyValueDataSource.class);
        indexDB = mock(DB.class);
        blockStoreCache = mock(BlockStoreCache.class);
        remascCache = mock(MaxSizeHashMap.class);

        target = new IndexedBlockStore(
                blockStoreEncoder,
                indexMap,
                keyValueDataSource,
                indexDB,
                blockStoreCache,
                remascCache
        );
    }

    /**
     * The header (not the block) is found and retrieved.
     */
    @Test
    public void getBlockHeaderByHash_success() {
        byte[] hash = new byte[32];
        byte[] headerResponse = {0x02, 0x02};

        BlockHeader blockHeaderDecoded = mock(BlockHeader.class);

        when(keyValueDataSource.get(hash)).thenReturn(headerResponse);
        when(blockStoreEncoder.decodeBlockHeader(headerResponse)).thenReturn(Optional.of(blockHeaderDecoded));

        BlockHeader result = target.getBlockHeaderByHash(hash);

        assertNotNull(result);
        assertEquals(blockHeaderDecoded, result);
    }

    /**
     * The block hash is not found.
     */
    @Test
    public void getBlockHeaderByHash_not_found() {
        byte[] hash = new byte[32];

        when(keyValueDataSource.get(hash)).thenReturn(null);

        BlockHeader result = target.getBlockHeaderByHash(hash);

        assertNull(result);
    }

    /**
     * When saving a block header it should NOT be written to cache if found.
     */
    @Test
    public void saveBlockHeader_contained_in_cache_success() {
        BlockHeader blockHeader = mock(BlockHeader.class);
        Keccak256 hash = mock(Keccak256.class);

        when(blockHeader.getHash()).thenReturn(hash);
        when(blockStoreCache.getBlockHeaderByHash(hash)).thenReturn(Optional.of(blockHeader));

        target.saveBlockHeader(blockHeader);

        verify(blockStoreCache, times(0)).addBlockHeader(hash, blockHeader);
    }

    /**
     * When saving a block header, it should be written to cache if not found.
     */
    @Test
    public void saveBlockHeader_cache_success() {
        BlockHeader blockHeader = mock(BlockHeader.class);
        Keccak256 hash = mock(Keccak256.class);

        when(blockHeader.getHash()).thenReturn(hash);
        when(blockStoreCache.getBlockHeaderByHash(hash)).thenReturn(Optional.empty());

        target.saveBlockHeader(blockHeader);

        verify(blockStoreCache, times(1)).addBlockHeader(hash, blockHeader);
    }


    /**
     * When saving a block header it should NOT be written to the data store if found.
     */
    @Test
    public void saveBlockHeader_contained_in_data_store_success() {
        BlockHeader blockHeader = mock(BlockHeader.class);
        Keccak256 hash = mock(Keccak256.class);
        byte[] hashBytes = new byte[32];
        byte[] encodedHeader = new byte[] {0x0F, 0x0A};

        when(blockHeader.getHash()).thenReturn(hash);
        when(hash.getBytes()).thenReturn(hashBytes);
        when(keyValueDataSource.get(hashBytes)).thenReturn(encodedHeader);

        target.saveBlockHeader(blockHeader);

        verify(keyValueDataSource, times(0)).put(hashBytes, encodedHeader);
    }

    /**
     * When saving a block header, it should be written to the data store if not found.
     */
    @Test
    public void saveBlockHeader_data_store_success() {
        BlockHeader blockHeader = mock(BlockHeader.class);
        Keccak256 hash = mock(Keccak256.class);
        byte[] hashBytes = new byte[32];
        byte[] encodedHeader = new byte[] {0x0F, 0x0A};

        when(blockHeader.getHash()).thenReturn(hash);
        when(hash.getBytes()).thenReturn(hashBytes);
        when(keyValueDataSource.get(hashBytes)).thenReturn(null);
        when(blockStoreEncoder.encodeBlockHeader(blockHeader)).thenReturn(encodedHeader);
        target.saveBlockHeader(blockHeader);

        verify(keyValueDataSource, times(1)).put(hashBytes, encodedHeader);
    }


    /**
     * When saving a block it should always be saved to the cache.
     */
    @Test
    public void saveBlock_cache_success() {
        Block block = mock(Block.class);
        Keccak256 hash = mock(Keccak256.class);

        when(block.getHash()).thenReturn(hash);

        target.saveBlock(block, mock(BlockDifficulty.class), true);

        verify(blockStoreCache, times(1)).addBlock(hash, block);
    }

    /**
     * When saving a block and nothing is found on the key value data store, it is stored in the data source.
     */
    @Test
    public void saveBlock_nothing_found() {
        Block block = mock(Block.class);
        Keccak256 hash = mock(Keccak256.class);
        byte[] hashBytes = new byte[32];
        byte[] encodedBlock = {0x0F, 0x0A};

        when(block.getHash()).thenReturn(hash);
        when(hash.getBytes()).thenReturn(hashBytes);
        when(blockStoreEncoder.encodeBlock(block)).thenReturn(encodedBlock);
        target.saveBlock(block, mock(BlockDifficulty.class), true);

        verify(keyValueDataSource, times(1)).put(hashBytes, encodedBlock);
    }

    /**
     * When saving a block and something that does not contain a block is found on the key value data store,
     * it is stored in the data source.
     */
    @Test
    public void saveBlock_header_found() {
        Block block = mock(Block.class);
        byte[] encodedBlock = {0x0F, 0x0F};
        Keccak256 hash = mock(Keccak256.class);
        byte[] hashBytes = new byte[32];
        byte[] encodedHeader = {0x0A, 0x0A};


        when(block.getHash()).thenReturn(hash);
        when(hash.getBytes()).thenReturn(hashBytes);
        when(blockStoreEncoder.encodeBlock(block)).thenReturn(encodedBlock);
        when(keyValueDataSource.get(hashBytes)).thenReturn(encodedHeader);
        when(blockStoreEncoder.decodeBlock(encodedHeader)).thenReturn(Optional.empty());
        target.saveBlock(block, mock(BlockDifficulty.class), true);

        verify(keyValueDataSource, times(1)).put(hashBytes, encodedBlock);
    }
}