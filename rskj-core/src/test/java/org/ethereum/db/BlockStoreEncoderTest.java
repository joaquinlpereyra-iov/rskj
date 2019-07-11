package org.ethereum.db;


import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.ethereum.core.Block;
import org.ethereum.core.BlockFactory;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.RLP;
import org.junit.Test;

import java.util.Optional;

public class BlockStoreEncoderTest {

    /**
     * Verifies that a block header value is decoded correctly.
     * A BlockStoreEncoder header encode is a RLP list with the encoded header as an element.
     */
    @Test
    public void decodeBlockHeader_success() {
        BlockFactory blockFactory = mock(BlockFactory.class);
        BlockStoreEncoder blockStoreEncoder = new BlockStoreEncoder(blockFactory);

        BlockHeader blockHeader = mock(BlockHeader.class);

        byte[] encodedHeader = new byte[] {0x0A};
        when(blockHeader.getEncoded()).thenReturn(encodedHeader);
        when(blockFactory.decodeHeader(encodedHeader)).thenReturn(blockHeader);
        byte [] rlpHeader = RLP.encodeList(encodedHeader);

        Optional<BlockHeader> result = blockStoreEncoder.decodeBlockHeader(rlpHeader);

        assertTrue(result.isPresent());
        assertArrayEquals(encodedHeader, result.get().getEncoded());
    }

    /**
     * Verifies that a block value is decoded correctly.
     * A BlockStoreEncoder block encode is the same as the block encode.
     */
    @Test
    public void decodeBlock_success() {
        BlockFactory blockFactory = mock(BlockFactory.class);
        BlockStoreEncoder blockStoreEncoder = new BlockStoreEncoder(blockFactory);

        Block block = mock(Block.class);

        byte [] rlpBlock = RLP.encodeList(new byte[]{0,1,2});
        when(blockFactory.decodeBlock(rlpBlock)).thenReturn(block);
        Optional<Block> result = blockStoreEncoder.decodeBlock(rlpBlock);

        assertTrue(result.isPresent());
        assertEquals(block, result.get());
    }

    /**
     * Verifies that a header is retrieved from a block encoding.
     */
    @Test
    public void decodeHeaderFromBlock_success() {
        BlockFactory blockFactory = mock(BlockFactory.class);
        BlockStoreEncoder blockStoreEncoder = new BlockStoreEncoder(blockFactory);

        Block block = mock(Block.class);
        BlockHeader blockHeader = mock(BlockHeader.class);
        when(block.getHeader()).thenReturn(blockHeader);

        byte [] rlpBlock = RLP.encodeList(new byte[]{0, 1, 2});
        when(blockFactory.decodeBlock(rlpBlock)).thenReturn(block);
        Optional<BlockHeader> result = blockStoreEncoder.decodeBlockHeader(rlpBlock);

        assertTrue(result.isPresent());
        assertEquals(blockHeader, result.get());
    }

    /**
     * Verifies that an invalid encoding throws exception.
     * Validity is only verified by the rlp list size.
     */
    @Test(expected = IllegalArgumentException.class)
    public void decodeBlockHeader_fails() {
        BlockFactory blockFactory = mock(BlockFactory.class);
        BlockStoreEncoder blockStoreEncoder = new BlockStoreEncoder(blockFactory);

        byte [] rlpBlock = RLP.encodeList(new byte[]{0, 1});
        blockStoreEncoder.decodeBlockHeader(rlpBlock);
    }

    /**
     * Verifies that an invalid encoding throws exception.
     * Validity is only verified by the rlp list size.
     */
    @Test(expected = IllegalArgumentException.class)
    public void decodeBlock_fails() {
        BlockFactory blockFactory = mock(BlockFactory.class);
        BlockStoreEncoder blockStoreEncoder = new BlockStoreEncoder(blockFactory);

        byte [] rlpBlock = RLP.encodeList(new byte[]{0, 1});
        blockStoreEncoder.decodeBlock(rlpBlock);
    }
}