package com.github.openthos.printer.localprint.task;

import java.util.List;

/**
 * Created by bboxh on 2016/5/16.
 */
public class DeletePrinterTask<Progress> extends CommandTask<String, Progress, Boolean> {


    @Override
    protected String[] setCmd(String[] name) {
        String printerName = name[0];
        return new String[]{"sh","proot.sh","lpadmin","-x",printerName};
    }

    @Override
    protected Boolean handleCommand(List<String> stdOut, List<String> stdErr) {
        boolean flag = true;
        // TODO: 2016/5/16 删除打印机 B4
        for(String line:stdErr){
            if (line.contains("The printer or class does not exist."))
                flag = false;
        }
        return flag;
    }

    @Override
    protected String bindTAG() {
        return "DeletePrinterTask";
    }
}
