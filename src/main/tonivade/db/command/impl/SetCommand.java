package tonivade.db.command.impl;

import tonivade.db.command.ICommand;
import tonivade.db.command.IRequest;
import tonivade.db.command.IResponse;
import tonivade.db.data.DataType;
import tonivade.db.data.DatabaseValue;
import tonivade.db.data.IDatabase;

/**
 *
 * @author tomby
 *
 */
public class SetCommand implements ICommand {

    @Override
    public void execute(IDatabase db, IRequest request, IResponse response) {
        DatabaseValue value = new DatabaseValue();
        value.setType(DataType.STRING);
        value.setValue(request.getParam(2));
        db.put(request.getParam(1), value);
        response.addSimpleStr(OK);
    }

}