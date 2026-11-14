package com.rpzsoft;

import com.rpzsoft.common.Conf;
import com.rpzsoft.http.HttpHandler;
import com.rpzsoft.http.WsHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;

/**
 * Hello world!
 *
 */
public class Server {
    private final Logger logger = LoggerFactory.getLogger(Server.class);
    static final Conf CONF = Conf.getInstance();

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    Channel ch;

    void start() throws Exception {
        final SslContext sslCtx;
        // Configure SSL. For prod, we must use proxy/LB infront of the server to terminate SSL.
        //here SSL is used only for testing
        if(Conf.SSL) {
            final String cafile = Conf.caPath();
            final String keyfile = Conf.keyPath();
            if(cafile.isEmpty() || keyfile.isEmpty()) {
                logger.info("Using selfsigned certificate!");
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
                //throw new IllegalStateException("Invalid certificate paths!");
            }else {
                sslCtx = SslContextBuilder.forServer(new FileInputStream(cafile),
                        new FileInputStream(keyfile)).sslProvider(SslProvider.JDK).build();
            }
        }else
            sslCtx = null;

        // Configure the server.
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            if (sslCtx != null) {
                                pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc()));
                            }

                            pipeline.addLast(new HttpServerCodec());
                            //      pipeline.addLast(new FlowControlHandler());
                            pipeline.addLast(new HttpObjectAggregator(1024*1024));
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new HttpContentCompressor());
                            //for websocket channels
                            //pipeline.addLast(new WebSocketServerCompressionHandler());
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, false, 1024*1024, false, true));
                            pipeline.addLast(new HttpHandler());
                            pipeline.addLast(new WsHandler());
                        }});

            ch = b.bind(Conf.HOST, Conf.PORT).sync().channel();

            System.out.println( CONF.getProperty("app.name")
                    + " Server is listening at: http"+(Conf.SSL?"s":"")+"://"+Conf.HOST+":"+Conf.PORT);
            System.out.println( );

            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

            ch.closeFuture().sync();
        }catch (Throwable t){
            logger.error("Unable to start Server on: "+Conf.HOST +":"+Conf.PORT, t);
            throw new IllegalStateException("Unable to start Server!!");
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void shutdown() {
        try{
            ch.close();
        }catch (Exception e1) {
            logger.error("Shutting down: ", e1);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main( String[] args ) throws Exception {
        new Server().start();
    }
}
