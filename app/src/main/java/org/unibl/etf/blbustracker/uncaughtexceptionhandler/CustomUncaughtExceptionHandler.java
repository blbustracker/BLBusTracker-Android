package org.unibl.etf.blbustracker.uncaughtexceptionhandler;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;

//Saves StackTrace of the crash if it occurs during RunTime
public class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
    public static final String STACK_TRACE_FILE_NAME = "crash.stacktrace";
    private Thread.UncaughtExceptionHandler defaultUEH;
    private Context context;

    public CustomUncaughtExceptionHandler(Context context)
    {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex)
    {
        StackTraceElement[] arr = ex.getStackTrace();
        StringBuilder report = new StringBuilder(ex.toString() + "\n\n");
        report.append("----------- Stack trace -----------\n\n");
        for (int i = 0; i < arr.length; i++)
        {
            report.append("    ").append(arr[i].toString().trim()).append("\n");
        }
        report.append("-----------------------------------\n\n");

        // Returns the cause of this throwable or null if the cause is nonexistent or unknow
        Throwable cause = ex.getCause();
        if (cause != null)
        {
            report.append("----------- Cause -----------\n\n");
            report.append(cause.toString()).append("\n\n");
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++)
            {
                report.append("    ").append(arr[i].toString().trim()).append("\n");
            }
            report.append("-----------------------------------\n\n");
        }

        //try with resources
        try(FileOutputStream trace = context.openFileOutput(STACK_TRACE_FILE_NAME, Context.MODE_PRIVATE))
        {
            trace.write(report.toString().getBytes());

        }catch (IOException ioe)
        {
            // ...
        }
        defaultUEH.uncaughtException(thread, ex);
    }
}