package com.dempe.ocean.common.codec.mqtt;

import com.dempe.ocean.common.protocol.mqtt.ConnAckMessage;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeMap;

import java.util.List;

/**
 * @author andrea
 */
class ConnAckDecoder extends DemuxDecoder {

    @Override
    void decode(AttributeMap ctx, ByteBuf in, List<Object> out) throws Exception {
        in.resetReaderIndex();
        //Common decoding part
        ConnAckMessage message = new ConnAckMessage();
        if (!decodeCommonHeader(message, 0x00, in)) {
            in.resetReaderIndex();
            return;
        }
        //skip reserved byte
        in.skipBytes(1);

        //read  return code
        message.setReturnCode(in.readByte());
        out.add(message);
    }

}
