package jack;

import com.jackpang.netty.AppClient;
import com.jackpang.netty.AppServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * description: NettyTest
 * date: 11/2/23 7:52 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class NettyTest {
    @Test
    public void testByteBuf() {
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();
        CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
        byteBuf.addComponents(header, body);
    }

    @Test
    public void testMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();
        message.writeBytes("ydl".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(1234567890L);
        AppClient appClient = new AppClient();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(appClient);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        message.writeBytes(byteArray);
        System.out.println(message.readableBytes());
    }

    @Test
    public void testCompress() throws IOException {
        byte[] buff = {12, 24, 32, 12, 32, 12, 32, 12};
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(buff);
        gzipOutputStream.flush();
        gzipOutputStream.finish();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        System.out.println(Arrays.toString(bytes));
        System.out.println(buff.length + "-->" + bytes.length);
    }

    @Test
    public void testDeCompress() throws IOException {
        byte[] buff = {31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -29, -111, 80, -32, 1, 65, 0, 107, -50, -6, -111, 8, 0, 0, 0 };
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buff);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        byte[] bytes = gzipInputStream.readAllBytes();
        System.out.println(Arrays.toString(bytes));
        System.out.println(buff.length + "-->" + bytes.length);
    }
}
