package org.ly.lysudoku.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;

import org.ly.lysudoku.R;

public class InputButton extends androidx.appcompat.widget.AppCompatButton {
    int num;
    Paint paint = getPaint();
    private int msNumColor;
    public void setSNumColor(int c) {
        msNumColor=c;
    }
    public int getSNumColor()
    {
        return msNumColor;
    }
    public InputButton(Context context) {
        this(context, null);
    }

    public InputButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudokuBoardView/*, defStyle, 0*/);
        setSNumColor(a.getColor(R.styleable.SudokuBoardView_textColorReadOnly, Color.GREEN));
    }

    public void setNum(int n)
    {
        num=n;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        try {

            if (!TextUtils.isEmpty(getText()) ) {
                String tag = getText().toString();
                //绘制第一行文字
                //paint.setColor(Color.rgb(0x00, 00, 0xff));
                //float tagWidth = paint.measureText(getText().toString());
                    Rect rect = new Rect();
                    paint.getTextBounds(tag, 0, tag.length(), rect);
                    int w = rect.width();
                    int h = rect.height();
                    int x = (int) (this.getWidth() - w)/2;
                    int y = (int)(this.getHeight()/2+h/2);
                    canvas.drawText(tag, x, y, paint);
                    if(num>0) {
                        //绘制第二行文字
                        Paint paint1 = new Paint();
                        paint1.setTextSize(paint.getTextSize() - 2);
                        paint1.setColor(msNumColor);
                        float numWidth = paint1.measureText(num + "");
                        int x1 = (int) (this.getWidth() - numWidth-16);
                        int y1 = y/2;
                        canvas.drawText(num + "", x1, y1, paint1);
                    }

            }
        }catch (Exception er)
        {
            er.printStackTrace();
        }
    }
}
