/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */

package tonivade.db;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import tonivade.db.redis.RedisToken;
import tonivade.db.redis.RequestDecoder;

public class TinyDBClient implements ITinyDB {

    private static final Logger LOGGER = Logger.getLogger(TinyDB.class.getName());

    private static final int BUFFER_SIZE = 1024 * 1024;
    private static final int MAX_FRAME_SIZE = BUFFER_SIZE * 100;

    private int port;
    private String host;
    private int reconnectionTime;

    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;

    private ChannelFuture future;

    private ChannelHandlerContext ctx;
    private TinyDBInitializerHandler initHandler;
    private TinyDBConnectionHandler connectionHandler;

    private ITinyDBCallback callback;

    public TinyDBClient(ITinyDBCallback callback) {
        this("localhost", 7081, callback);
    }

    public TinyDBClient(String host, int port, ITinyDBCallback callback) {
        this.host = host;
        this.port = port;
        this.reconnectionTime = 10;
        this.callback = callback;
    }

    public void start() {
        workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        initHandler = new TinyDBInitializerHandler(this);
        connectionHandler = new TinyDBConnectionHandler(this);

        bootstrap = new Bootstrap()
          .group(workerGroup)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
          .option(ChannelOption.SO_RCVBUF, BUFFER_SIZE)
          .option(ChannelOption.SO_SNDBUF, BUFFER_SIZE)
          .option(ChannelOption.SO_KEEPALIVE, true)
          .handler(initHandler);

        try {
            connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            if (future != null) {
                future.channel().close();
            }
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private void connect() {
        LOGGER.info(() -> "trying to reconnect");

        future = bootstrap.connect(host, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.channel().close();

                    workerGroup.schedule(new Runnable() {
                        @Override
                        public void run() {
                            connect();
                        }
                    }, reconnectionTime, TimeUnit.SECONDS);
                } else {
                    LOGGER.info(() -> "successful connection");
                }
            }
        });
    }

    @Override
    public void channel(SocketChannel channel) {
        LOGGER.fine(() -> "connected to server");

        channel.pipeline().addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
        channel.pipeline().addLast("linDelimiter", new RequestDecoder(MAX_FRAME_SIZE));
        channel.pipeline().addLast(connectionHandler);
    }

    @Override
    public void connected(ChannelHandlerContext ctx) {
        LOGGER.info(() -> "channel active");

        this.ctx = ctx;

        callback.onConnect();
    }

    @Override
    public void disconnected(ChannelHandlerContext ctx) {
        if (this.ctx != null) {
            LOGGER.info(() -> "client disconected from server");

            this.ctx = null;

            callback.onDisconnect();

            // reconnect
            connect();
        }
    }

    public void send(String message) {
        if (ctx != null) {
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void receive(ChannelHandlerContext ctx, RedisToken message) {
        callback.onMessage(message);
    }

}
