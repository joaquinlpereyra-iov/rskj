package org.ethereum.db;

import java.util.Optional;

import co.rsk.util.Either;
import org.ethereum.core.Block;
import org.ethereum.core.BlockFactory;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

/**
 * Encodes and decodes both block headers and blocks allowing both value types to be stored the same block store.
 */
public class BlockStoreEncoder {

    private final BlockFactory blockFactory;

    /**
     * Creates a new block store encoder.
     *
     * @param blockFactory The block factory is needed to decode encoded values, cannot be null.
     */
    public BlockStoreEncoder(BlockFactory blockFactory) {
        this.blockFactory = blockFactory;
    }

    /**
     * Encodes a header to store in the block store.
     *
     * @param blockHeader The block header to encode, cannot be null.
     * @return The encoded header, never null.
     */
    public byte[] encodeBlockHeader(BlockHeader blockHeader) {
        if (blockHeader == null) {
            throw new IllegalArgumentException("Block header to wrap cannot be null");
        }

        return RLP.encodeList(blockHeader.getEncoded());
    }

    /**
     * Encodes a block to store in the block store or the cache.
     *
     * @param block The block to encode, cannot be null.
     * @return The encoded block, never null.
     */
    public byte[] encodeBlock(Block block) {
        if (block == null) {
           throw new IllegalArgumentException("Block to wrap cannot be null");
        }

        return block.getEncoded();
    }


    /**
     * Decodes an encoded block or header value and generates either a block or a header depending on the
     * stored bytes.
     *
     * @param value A value encoded as an RLP list.
     * A block raw data rlp list must have 3 elements, a header, a transaction list and an uncle headers list.
     * A header raw data rlp list only has the header element.
     *
     * @return A sealed block or block header decoded from the raw data.
     */
    private Either<Block, BlockHeader> decodeValue(byte[] value) {
        RLPList rlpValue = RLP.decodeList(value);
        if (rlpValue.size() == 3) {
            return Either.left(blockFactory.decodeBlock(value));
        } else if (rlpValue.size() == 1) {
            return Either.right(blockFactory.decodeHeader(rlpValue.get(0).getRLPData()));
        }
        throw new IllegalArgumentException("Wrapped value doesn't correspond to valid block nor header");
    }

    /**
     * Decodes a valid encoded block or block header and retrieves the block if possible.
     *
     * @param value A valid encoded block or block header.
     * @return An optional block, empty if the value doesn't decode to a block.
     */
    public Optional<Block> decodeBlock(byte[] value) {
        return this.decodeValue(value).either(Optional::of, blockHeader -> Optional.empty());
    }

    /**
     * Decodes a valid encoded block or block header and retrieves the header if possible.
     *
     * @param value A valid encoded block or block header.
     * @return An optional block header. Never be empty.
     */
    public Optional<BlockHeader> decodeBlockHeader(byte[] value) {
        return Optional.of(this.decodeValue(value).either(Block::getHeader, header -> header));
    }
}
