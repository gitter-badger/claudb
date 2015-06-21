/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */

package tonivade.db.command.server;

import static tonivade.db.redis.SafeString.fromString;

import org.junit.Rule;
import org.junit.Test;

import tonivade.db.command.CommandRule;
import tonivade.db.command.CommandUnderTest;

@CommandUnderTest(PingCommand.class)
public class PingCommandTest {

    @Rule
    public final CommandRule rule = new CommandRule(this);

    @Test
    public void testExecute() {
        rule.execute()
            .verify().addSimpleStr("PONG");
    }

    @Test
    public void testExecuteWithParam() {
        rule.withParams("Hi!")
            .execute()
            .verify().addBulkStr(fromString("Hi!"));
    }

}
