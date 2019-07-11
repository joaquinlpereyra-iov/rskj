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
import co.rsk.util.MaxSizeHashMap;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import co.rsk.util.Either;

import java.util.Map;
import java.util.Optional;

/**
 * BlockStoreCache stores both blocks and block headers and has a set max size. Elements are removed in access order.
 */
public class BlockStoreCache {
    private final Map<Keccak256, Either<Block, BlockHeader>> blockMap;

    public BlockStoreCache(int cacheSize) {
        this.blockMap = new MaxSizeHashMap<>(cacheSize, true);
    }

    public void removeValue(Keccak256 hash) {
        blockMap.remove(hash);
    }

    /**
     * Adds a block to the cache, any other block or header stored in the same key is overwritten.
     *
     * @param hash The look up key, cannot be null.
     * @param value The block to store, cannot be null.
     */
    public void addBlock(Keccak256 hash, Block value) {
        blockMap.put(hash, Either.left(value));
    }

    /**
     * Adds a block header to the cache, any other block or header stored in the same key is overwritten.
     *
     * @param hash The look up key, cannot be null.
     * @param value The header to store, cannot be null.
     */
    public void addBlockHeader(Keccak256 hash, BlockHeader value) {
        blockMap.put(hash, Either.right(value));
    }

    private Optional<Either<Block, BlockHeader>> getValueByHash(Keccak256 hash) {
        return Optional.ofNullable(blockMap.get(hash));
    }

    /**
     * Retrieves a block.
     *
     * @param hash The look up key, cannot be null.
     * @return An optional, empty if the block was not found.
     */
    public Optional<Block> getBlockByHash(Keccak256 hash) {
        return getValueByHash(hash).map(either -> either.either(block -> block, blockHeader -> null));
    }

    /**
     * Retrieves a header from the cache. If a block is found instead of a header, the block's header is returned.
     *
     * @param hash The look up key, cannot be null.
     * @return An optional, empty if the header was not found in the cache.
     */
    public Optional<BlockHeader> getBlockHeaderByHash(Keccak256 hash) {
        return getValueByHash(hash).map(either -> either.either(Block::getHeader, blockHeader -> blockHeader));
    }
}
