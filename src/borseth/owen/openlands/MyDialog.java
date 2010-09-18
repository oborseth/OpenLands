package borseth.owen.openlands;

import android.app.Dialog;
import android.content.Context;

public class MyDialog extends Dialog
{
    public MyDialog(final Context context)
    {
        // Set your theme here
        super(context, R.style.ParchmentDialogTheme);

        // This is the layout XML file that describes your Dialog layout
        this.setContentView(R.layout.dialog);  
    }
}