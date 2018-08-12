package neto.com.mx.conteocedisciclicos.dialogos;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

import neto.com.mx.conteocedisciclicos.R;

/**
 * Created by yruizm on 28/09/17.
 */

public class BienvenidaDialog extends Dialog {

    private Context context;
    public BienvenidaDialog(Context context) {
        super(context);
        this.context = context;
    }

    public void showDialog(Activity activity, String usuario){
        showDialog(activity, usuario, null);
    }
    public void showDialog(Activity activity, String usuario, OnDismissListener dismissListener){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.activity_dialogo_bienvenida);
        dialog.setOnDismissListener(dismissListener);

        TextView text = (TextView) dialog.findViewById(R.id.nombreEmpleadoText);
        text.setText(usuario);

        dialog.show();

        // Hide after some seconds
        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        };

        handler.postDelayed(runnable, 3000);

    }
}
