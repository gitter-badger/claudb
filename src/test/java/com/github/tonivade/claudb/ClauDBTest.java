/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.claudb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.function.Consumer;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class ClauDBTest {

  @Rule
  public final ClauDBRule rule = new ClauDBRule();

  @Test
  public void testCommands() {
    execute(jedis -> {
      assertThat(jedis.ping(), equalTo("PONG"));
      assertThat(jedis.echo("Hi!"), equalTo("Hi!"));
      assertThat(jedis.set("a", "1"), equalTo("OK"));
      assertThat(jedis.strlen("a"), equalTo(1L));
      assertThat(jedis.strlen("b"), equalTo(0L));
      assertThat(jedis.exists("a"), equalTo(true));
      assertThat(jedis.exists("b"), equalTo(false));
      assertThat(jedis.get("a"), equalTo("1"));
      assertThat(jedis.get("b"), nullValue());
      assertThat(jedis.getSet("a", "2"), equalTo("1"));
      assertThat(jedis.get("a"), equalTo("2"));
      assertThat(jedis.del("a"), equalTo(1L));
      assertThat(jedis.get("a"), nullValue());
      assertThat(jedis.eval("return 1"), equalTo(1L));
      assertThat(jedis.quit(), equalTo("OK"));
    });
  }

  @Test
  public void testPipeline() {
    execute(jedis -> {
      Pipeline p = jedis.pipelined();
      p.ping();
      p.echo("Hi!");
      p.set("a", "1");
      p.strlen("a");
      p.strlen("b");
      p.exists("a");
      p.exists("b");
      p.get("a");
      p.get("b");
      p.getSet("a", "2");
      p.get("a");
      p.del("a");
      p.get("a");

      Iterator<Object> result = p.syncAndReturnAll().iterator();
      assertThat(result.next(), equalTo("PONG"));
      assertThat(result.next(), equalTo("Hi!"));
      assertThat(result.next(), equalTo("OK"));
      assertThat(result.next(), equalTo(1L));
      assertThat(result.next(), equalTo(0L));
      assertThat(result.next(), equalTo(true));
      assertThat(result.next(), equalTo(false));
      assertThat(result.next(), equalTo("1"));
      assertThat(result.next(), nullValue());
      assertThat(result.next(), equalTo("1"));
      assertThat(result.next(), equalTo("2"));
      assertThat(result.next(), equalTo(1L));
      assertThat(result.next(), nullValue());

      jedis.quit();
    });
  }

  @Test
  public void testEval()
  {
    execute(jedis -> assertThat(jedis.eval("return 1"), equalTo(1L)));
  }

  @Test
  public void testEvalScript()
  {
    String script = "local keys = redis.call('keys', '*region*') "
        + "for i,k in ipairs(keys) do local res = redis.call('del', k) end";
    execute(jedis -> assertThat(jedis.eval(script), nullValue()));
  }

  @Test
  @Ignore
  public void testLoad100000() {
    execute(jedis -> {
      int times = 100000;
      long start = System.nanoTime();
      for (int i = 0; i < times; i++) {
        jedis.set(key(i), value(i));
      }
      jedis.quit();
      assertThat((System.nanoTime() - start) / times, lessThan(1000000L));
    });
  }

  private void execute(Consumer<Jedis> action) {
    try (Jedis jedis = createClientConnection()) {
      action.accept(jedis);
    }
  }

  private Jedis createClientConnection() {
    return new Jedis(DBServerContext.DEFAULT_HOST, DBServerContext.DEFAULT_PORT, 10000);
  }

  private String value(int i) {
    return "value" + String.valueOf(i);
  }

  private String key(int i) {
    return "key" + String.valueOf(i);
  }
}
