package in.co.praveenkumar.bard.activities;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Button;

import in.co.praveenkumar.bard.R;

/**
 * Created by Azizul Hakim on 6/5/15.
 * azizulfahim2002@gmail.com
 */
public class BeagleButton extends Button{
    private int keyCodeIndex = 0;
    public BeagleButton(Context context) {
        super(context);
    }

    public BeagleButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.BeaggleButtonWidget);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i)
        {
            int attr = a.getIndex(i);
            switch (attr)
            {
                case R.styleable.BeaggleButtonWidget_keyCodeIndex:
                    this.keyCodeIndex = a.getInt(attr, 0);
                    break;

                default:
                    break;
            }
        }
        a.recycle();
    }

    public BeagleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.BeaggleButtonWidget);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i)
        {
            int attr = a.getIndex(i);
            switch (attr)
            {
                case R.styleable.BeaggleButtonWidget_keyCodeIndex:
                    this.keyCodeIndex = a.getInt(attr, 0);
                    break;

                default:
                    break;
            }
        }
        a.recycle();
    }

    public int getKeyCodeIndex(){
        return keyCodeIndex;
    }
}
