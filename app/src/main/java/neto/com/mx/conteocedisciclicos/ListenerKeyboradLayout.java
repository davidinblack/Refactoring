package neto.com.mx.conteocedisciclicos;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by dramirezr on 20/02/2018.
 */

public class ListenerKeyboradLayout extends RelativeLayout {
    int r_layout_id = -1;
    ISoftKeyboardHiddenChanged handlerExterno;
    public ListenerKeyboradLayout(Context context, AttributeSet attrs, int r_layout_id, ISoftKeyboardHiddenChanged handler) {
        super(context, attrs);
        handlerExterno = handler;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate( r_layout_id, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int heightPropuesto = MeasureSpec.getSize(heightMeasureSpec);
        final int heightActual = getHeight();
        // Si heightActual > heightPropuesto = softKeyboard mostrado
        handlerExterno.softKeyboardHiddenChanged(heightActual > heightPropuesto );
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public interface ISoftKeyboardHiddenChanged{
        void softKeyboardHiddenChanged(boolean visible);
    }
}
