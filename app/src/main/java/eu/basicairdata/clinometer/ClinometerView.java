/*
 * ClinometerView - Java Class for Android
 * Created by G.Capelli (BasicAirData) on 21/5/2020
 *
 * This file is part of BasicAirData Clinometer for Android.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */


package eu.basicairdata.clinometer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ClinometerView extends View {

    private static final float TEXT_ALIGNMENT_LEFT = 0.0f;
    private static final float TEXT_ALIGNMENT_CENTER = 0.5f;
    private static final float TEXT_ALIGNMENT_RIGHT = 1.0f;
    private static final float TEXT_ALIGNMENT_TOP = 1.0f;
    private static final float TEXT_ALIGNMENT_BOTTOM = 0.0f;
    private static final float TEXT_ROTATION_0 = 0.0f;
    private static final float TEXT_ROTATION_90 = 90.0f;
    private static final float TEXT_ROTATION_180 = 180.0f;
    private static final float TEXT_ROTATION_270 = 270.0f;

    private static final float N_CIRCLES_FULLY_VISIBLE = 4.5f;

    ClinometerActivity SVActivity = ClinometerActivity.getInstance();

    private Paint paint_bg;                 // For Background Gradient
    private Paint paint_LTGray;             // For Background Lines 30° + Circles
    private Paint paint_DKGray;             // For Background Lines != 30°
    private Paint paint_White;              // For White Angles Lines
    private Paint paint_WhiteText;          // For White Angles Text
    private Paint paint_ShadowText;         // For Shadows of Text
    private Paint paint_Arc;                // For White Angles Arcs
    private Paint paint_Yellow_Spirit;      // For Lines and Spirit Bubbles
    private Paint paint_bg_horizon;         // For Horizon Background

    Rect textbounds = new Rect();

    int x;                      // The Width of Screen
    int y;                      // The Height of Screen
    int min_xy;                 // The minimum between Width and Height
    int max_xy;                 // The maximum between Width and Height
    int xc;                     // x screen center
    int yc;                     // y screen center
    double diag2c;              // Screen Diagonal/2 = distance between 0:0 and xc:yc
    int ncircles;               // The number of visible circles
    float xs;                   // The X Coordinate of the spirit bubble
    float ys;                   // The Y Coordinate of the spirit bubble
    float r1_value;             // The scale (to how many degrees corresponds each circle)
    float r1;                   // The radius of the first circle = 1 deg.

    float rot_angle_rad;            // The angle of rotation between absolute 3 o'clock and the white axe
    float horizon_angle_deg;        // Horizon angle

    float Angle1_start;         // The Arc 1 start
    float Angle2_start;         // The Arc 2 start
    float Angle1_extension;     // The Arc 1 angle (+)
    float Angle2_extension;     // The Arc 2 angle (-)

    int RefAxe = 0;             // The reference axe for white Angles
                                // 0  = Horizontal axe
                                // 90 = Vertical Axe

    RectF ArcRectF = new RectF();

    private boolean isAngle2LabelOnLeft = true;                 // True if the label of the Angle[2] must be placed on left instead of right
    private static final int ANGLE2LABELSWITCH_THRESHOLD = 2;   // 2 Degrees of Threshold for switching L/R the Angle[2] label
    private boolean isShaderCreated = false;                    // True if the Background Shader has been created


    public ClinometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        CreatePaints();

    }


    public ClinometerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        CreatePaints();

    }


    public ClinometerView(Context context) {
        super(context);
        CreatePaints();

    }



    private void CreatePaints() {
        // create the Paint and set its color
        paint_LTGray = new Paint();
        paint_LTGray.setColor(getResources().getColor(R.color.line_light));
        paint_LTGray.setStyle(Paint.Style.STROKE);
        paint_LTGray.setDither(true);
        paint_LTGray.setAntiAlias(true);

        paint_White = new Paint();
        paint_White.setColor(getResources().getColor(R.color.line_white));
        paint_White.setStyle(Paint.Style.STROKE);
        paint_White.setStrokeWidth(1.5f);
        paint_White.setDither(true);
        paint_White.setAntiAlias(true);

        paint_WhiteText = new Paint();
        paint_WhiteText.setColor(getResources().getColor(R.color.line_white));
        paint_WhiteText.setStyle(Paint.Style.FILL);
        paint_WhiteText.setDither(true);
        paint_WhiteText.setAntiAlias(true);
        paint_WhiteText.setTextSize(getResources().getDimensionPixelSize(R.dimen.myFontSize));
        paint_WhiteText.setFakeBoldText(true);

        paint_ShadowText = new Paint();
        paint_ShadowText.setColor(getResources().getColor(R.color.black_overlay));
        paint_ShadowText.setStyle(Paint.Style.STROKE);
        paint_ShadowText.setStrokeWidth(5);
        paint_ShadowText.setDither(true);
        paint_ShadowText.setAntiAlias(true);
        paint_ShadowText.setTextSize(getResources().getDimensionPixelSize(R.dimen.myFontSize));
        paint_ShadowText.setFakeBoldText(true);

        paint_Arc = new Paint();
        paint_Arc.setColor(getResources().getColor(R.color.line_white));
        paint_Arc.setStyle(Paint.Style.STROKE);
        paint_Arc.setStrokeWidth(3);
        paint_Arc.setDither(true);
        paint_Arc.setAntiAlias(true);

        paint_DKGray = new Paint();
        paint_DKGray.setColor(getResources().getColor(R.color.line_dark));
        paint_DKGray.setStyle(Paint.Style.STROKE);
        paint_DKGray.setDither(true);
        paint_DKGray.setAntiAlias(true);

        paint_bg = new Paint();
        paint_bg.setStyle(Paint.Style.FILL);
        isShaderCreated = false;

        paint_Yellow_Spirit = new Paint();
        paint_Yellow_Spirit.setStyle(Paint.Style.FILL);
        paint_Yellow_Spirit.setStrokeWidth(3);
        paint_Yellow_Spirit.setDither(true);
        paint_Yellow_Spirit.setAntiAlias(true);
        paint_Yellow_Spirit.setTextSize(getResources().getDimensionPixelSize(R.dimen.myFontSize));
        paint_Yellow_Spirit.setFakeBoldText(true);
        paint_Yellow_Spirit.setColor(getResources().getColor(R.color.colorAccent));

        paint_bg_horizon = new Paint();
        paint_bg_horizon.setStyle(Paint.Style.FILL);
        paint_bg_horizon.setColor(getResources().getColor(R.color.bghorizon));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // --------[ CALCULATIONS ]-----------------------------------------------------------------

        x = getWidth();
        y = getHeight();
        min_xy = Math.min(x, y);
        max_xy = Math.max(x, y);
        xc = x / 2;                                     // x screen center
        yc = y / 2;                                     // y screen center
        diag2c = Math.sqrt(xc * xc + yc * yc);          // Screen Diagonal/2 = distance between 0:0 and xc:yc
        r1_value = 2;                                   // The scale (to how many degrees corresponds each circle)
        ncircles = (int) Math.ceil(N_CIRCLES_FULLY_VISIBLE * 2 * diag2c / min_xy);
                                                        // The number of circles to be drawn
        r1 = (min_xy / 2) / N_CIRCLES_FULLY_VISIBLE;    // The radius of the first circle.

        xs = xc + SVActivity.angle[0] * r1 / r1_value;  // The X coordinate of the spirit bubble center
        ys = yc - SVActivity.angle[1] * r1 / r1_value;  // The X coordinate of the spirit bubble center

        rot_angle_rad = (float) Math.toRadians(SVActivity.angleXY);
                                                        // The angle of rotation between absolute 3 o'clock and the white axe
        horizon_angle_deg = SVActivity.angleXY + 90;    // The angle of rotation between absolute 3 o'clock and the white axe

        if (RefAxe == 0) {
            Angle1_start = 0;
            Angle1_extension = horizon_angle_deg % 180;
            Angle2_start = 180;
            Angle2_extension = -(180 - (horizon_angle_deg % 180));
        }
        if (RefAxe == 90) {
            Angle1_start = 90;
            Angle1_extension = (horizon_angle_deg + 90) % 180;
            Angle2_start = 270;
            Angle2_extension = -(180 - ((horizon_angle_deg + 90) % 180));
        }



        // -----------------------------------------------------------------------------------------
        // --------[ BACKGROUND ]-------------------------------------------------------------------

        if (!isShaderCreated) {
            paint_bg.setShader(new RadialGradient(xc, yc, (int) (Math.sqrt(xc * xc + yc * yc) / 2),
                    getResources().getColor(R.color.bgpaint_dark),
                    getResources().getColor(R.color.bgpaint_light),
                    Shader.TileMode.MIRROR));
        }
        canvas.drawCircle(xc, yc, (int) Math.sqrt(xc*xc + yc*yc), paint_bg);

        // --------[ BACKGROUND OF SPIRIT LEVEL HORIZON ]-------------------------------------------

        canvas.save();
        canvas.rotate(SVActivity.angleXY + 90, xc, yc);
        //canvas.translate(0, (int) ((90 - Math.toDegrees(SVActivity.angleXYZ)) * r1 / r1_value));
        canvas.drawRect((int) (xc - diag2c), yc + (int) ((90 - SVActivity.angleXYZ) * r1 / r1_value),
                (int)(xc + diag2c), (int)(yc + diag2c), paint_bg_horizon);
        canvas.restore();

        // --------[ BACKGROUND LINES ]-------------------------------------------------------------

        for (int angle = 0; angle < 360; angle += 10) {
            canvas.drawLine(
                    xc - (int) (diag2c * Math.cos(Math.toRadians(angle))),
                    yc - (int) (diag2c * Math.sin(Math.toRadians(angle))),
                    xc - (int) ((angle % 90 == 0 ? 0 : r1) * Math.cos(Math.toRadians(angle))),
                    yc - (int) ((angle % 90 == 0 ? 0 : r1) * Math.sin(Math.toRadians(angle))),
                    angle % 30 == 0 ? paint_LTGray : paint_DKGray);
        }

        // --------[ HORIZONTAL AND VERTICAL AXIS ]----------------------------------------------------------------------

        if (RefAxe == 0) canvas.drawLine(0, yc, x, yc, paint_White);
        if (RefAxe == 90) canvas.drawLine(xc, 0, xc, y, paint_White);

        // --------[ BACKGROUND CIRCLES ]-----------------------------------------------------------

        for (int i = 1; i <= ncircles; i=i+1) canvas.drawCircle(xc, yc, Math.round(r1*i), paint_LTGray);
        //for (int i = 2; i <= ncircles*2; i=i+2) canvas.drawCircle(xc, yc, Math.round(r1*i), paint);
        //for (int i = 3; i <= ncircles*2; i=i+2) canvas.drawCircle(xc, yc, Math.round(r1*i), paint_secondary);



        // -----------------------------------------------------------------------------------------
        // --------[ SPIRIT LEVEL ]-----------------------------------------------------------------

        // Horizon and max gradient
        canvas.save();
        canvas.rotate((float) Math.toDegrees(rot_angle_rad), xc, yc);
        //canvas.drawLine(xc - (xc + yc), yc, xc + (xc + yc), yc, paint_LTGray);    // Max Gradient
        canvas.drawLine(xc - min_xy/2 + r1/2, yc, (float)-diag2c, yc, paint_White);                 // Max Gradient
        //canvas.drawLine(xc - min_xy/2 + r1, yc, xc, yc, paint_LTGray);                 // Max Gradient
        canvas.rotate(90, xc, yc);
        canvas.drawLine(xc - (xc + yc), yc, xc + (xc + yc), yc, paint_White);       // Horizon
        canvas.restore();

        // Cross
        canvas.drawLine(0, ys, x, ys, paint_Yellow_Spirit);
        canvas.drawLine(xs, 0, xs, y, paint_Yellow_Spirit);

        // White angles
        float r;

        r = 1.9f * r1;
        ArcRectF.left = xc - r;           // The RectF for the Arc
        ArcRectF.right = xc + r;
        ArcRectF.top = yc - r;
        ArcRectF.bottom = yc + r;
        canvas.drawArc(ArcRectF,Angle1_start + 2, Angle1_extension - 4, false, paint_White);

        r = 2.1f * r1;
        ArcRectF.left = xc - r;           // The RectF for the Arc
        ArcRectF.right = xc + r;
        ArcRectF.top = yc - r;
        ArcRectF.bottom = yc + r;
        canvas.drawArc(ArcRectF,Angle2_start - 2, Angle2_extension + 4, false, paint_White);

        // Bubble Circle
        canvas.drawCircle(xs, ys,r1/4, paint_Yellow_Spirit);

        // Spirit level Horizon
        if (!SVActivity.isFlat) {
            canvas.save();
            canvas.rotate(SVActivity.angleXY + 90, xc, yc);
            //canvas.translate(0, (int) ((90 - SVActivity.angleXYZ) * r1 / r1_value));
            canvas.drawLine((int) (xc - diag2c), yc + (int) ((90 - SVActivity.angleXYZ) * r1 / r1_value),
                    (int)(xc + diag2c), yc + (int) ((90 - SVActivity.angleXYZ) * r1 / r1_value), paint_Yellow_Spirit);
            canvas.restore();

            //canvas.drawLine(x_horizon[0], y_horizon[0], x_horizon[1], y_horizon[1], paint_Yellow_Spirit);
            //canvas.drawCircle(xs_vertical, ys_vertical,r1/4, paint_Yellow_Spirit);
        }



        // -----------------------------------------------------------------------------------------
        // --------[ TEXT LABELS ]------------------------------------------------------------------

        // Angle Z
        canvas.save();
        canvas.rotate( (float) Math.toDegrees(rot_angle_rad) + 180, xc, yc);
        DrawTextWithShadow(canvas, String.format("%1.1f°", Math.abs(90 - SVActivity.angle[2])),
                (int) (min_xy - (r1)), yc,
                TEXT_ALIGNMENT_CENTER, TEXT_ALIGNMENT_CENTER,
                (SVActivity.angleTextLabels - (float) Math.toDegrees(rot_angle_rad) - 180) , paint_WhiteText);
        canvas.restore();

        // Angle 0 + 1
        if (SVActivity.DisplayRotation == 0) {
            DrawTextWithShadow(canvas, String.format("%1.1f°", SVActivity.angle[0]), (int)xs - 20, y - 20,
                    TEXT_ALIGNMENT_RIGHT, TEXT_ALIGNMENT_BOTTOM, TEXT_ROTATION_0, paint_Yellow_Spirit);
            DrawTextWithShadow(canvas, String.format("%1.1f°", SVActivity.angle[1]), 20, (int)ys - 20,
                    TEXT_ALIGNMENT_LEFT, TEXT_ALIGNMENT_BOTTOM, TEXT_ROTATION_0, paint_Yellow_Spirit);
        }
        if (SVActivity.DisplayRotation == 90) {
            DrawTextWithShadow(canvas, String.format("%1.1f°", SVActivity.angle[0]), (int)xs + 20, 20,
                    TEXT_ALIGNMENT_LEFT, TEXT_ALIGNMENT_BOTTOM, TEXT_ROTATION_90, paint_Yellow_Spirit);
            DrawTextWithShadow(canvas, String.format("%1.1f°", SVActivity.angle[1]), 20, (int)ys - 20,
                    TEXT_ALIGNMENT_RIGHT, TEXT_ALIGNMENT_BOTTOM, TEXT_ROTATION_90, paint_Yellow_Spirit);
        }
        if (SVActivity.DisplayRotation == 180) {
            DrawTextWithShadow(canvas, String.format("%1.1f°", SVActivity.angle[0]), (int)xs + 20, 20,
                    TEXT_ALIGNMENT_RIGHT, TEXT_ALIGNMENT_BOTTOM, TEXT_ROTATION_180, paint_Yellow_Spirit);
            DrawTextWithShadow(canvas, String.format("%1.1f°", SVActivity.angle[1]), x - 20, (int)ys + 20,
                    TEXT_ALIGNMENT_LEFT, TEXT_ALIGNMENT_BOTTOM, TEXT_ROTATION_180, paint_Yellow_Spirit);
        }
        if (SVActivity.DisplayRotation == 270) {
            DrawTextWithShadow(canvas, String.format("%1.1f°", SVActivity.angle[0]), (int)xs - 20, y - 20,
                    TEXT_ALIGNMENT_LEFT, TEXT_ALIGNMENT_BOTTOM, TEXT_ROTATION_270, paint_Yellow_Spirit);
            DrawTextWithShadow(canvas, String.format("%1.1f°", SVActivity.angle[1]), x - 20, (int)ys + 20,
                    TEXT_ALIGNMENT_RIGHT, TEXT_ALIGNMENT_BOTTOM, TEXT_ROTATION_270, paint_Yellow_Spirit);
        }

        // Angle 2
        if (!SVActivity.isFlat) {
            if ((Math.abs(SVActivity.angle[2]) > ANGLE2LABELSWITCH_THRESHOLD)
                    && (Math.abs(SVActivity.angle[0]) > ANGLE2LABELSWITCH_THRESHOLD)
                    && (Math.abs(SVActivity.angle[1]) > ANGLE2LABELSWITCH_THRESHOLD)) {
                // Switch evaluation
                isAngle2LabelOnLeft = false;
                if (((SVActivity.DisplayRotation == 0) || (SVActivity.DisplayRotation == 180)) &&
                        (SVActivity.angle[2] * SVActivity.angle[0] < 0)) isAngle2LabelOnLeft = true;
                if (((SVActivity.DisplayRotation == 90) || (SVActivity.DisplayRotation == 270)) &&
                        (SVActivity.angle[2] * SVActivity.angle[1] < 0)) isAngle2LabelOnLeft = true;
                if ((SVActivity.DisplayRotation == 180) || (SVActivity.DisplayRotation == 270)) isAngle2LabelOnLeft = !isAngle2LabelOnLeft;
            }

            canvas.save();

            if (SVActivity.DisplayRotation == 0) {
                canvas.rotate(SVActivity.angle[0], xc, yc);
            }
            if (SVActivity.DisplayRotation == 90) {
                canvas.rotate(- 270 - SVActivity.angle[1], xc, yc);
            }
            if (SVActivity.DisplayRotation == 180) {
                canvas.rotate(180 - SVActivity.angle[0], xc, yc);
            }
            if (SVActivity.DisplayRotation == 270) {
                canvas.rotate(+ 270 + SVActivity.angle[1], xc, yc);
            }

            //canvas.drawLine(xc-(xc+yc), yc, xc+(xc+yc), yc, paint_White);
            canvas.translate(0, SVActivity.angle[2] * r1 / r1_value);

            if (isAngle2LabelOnLeft) {
                // SX
                DrawTextWithShadow(canvas, String.format("%1.1f°", SVActivity.angle[2]), 20, yc - 20,
                        TEXT_ALIGNMENT_LEFT, TEXT_ALIGNMENT_BOTTOM, TEXT_ROTATION_0, paint_Yellow_Spirit);
            } else {
                // DX
                DrawTextWithShadow(canvas, String.format("%1.1f°", SVActivity.angle[2]), x - 20 , yc - 20,
                        TEXT_ALIGNMENT_RIGHT, TEXT_ALIGNMENT_BOTTOM, TEXT_ROTATION_0, paint_Yellow_Spirit);
            }
            canvas.restore();
        }

        // --------[ WHITE LABELS ]-----------------------------------------------------------------

        canvas.save();
        canvas.rotate( Angle1_start + Angle1_extension/2, xc, yc);
        DrawTextWithShadow(canvas, String.format("%1.1f°", Math.abs(Angle1_extension)),
                (int) (xc + (r1 * 2) + 30 + paint_White.measureText("100.0°") / 2), yc,
                TEXT_ALIGNMENT_CENTER, TEXT_ALIGNMENT_CENTER,
                -Angle1_extension/2 - RefAxe + SVActivity.angleTextLabels , paint_WhiteText);
        canvas.rotate( 90 , xc, yc);
        DrawTextWithShadow(canvas, String.format("%1.1f°", Math.abs(Angle2_extension)),
                (int) (xc + (r1 * 2.1) + 30 + paint_White.measureText("100.0°") / 2), yc,
                TEXT_ALIGNMENT_CENTER, TEXT_ALIGNMENT_CENTER,
                -Angle1_extension/2 - 90 - RefAxe + SVActivity.angleTextLabels, paint_WhiteText);
        canvas.restore();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("SpiritLevel", "Center Screen " + xc + " " + yc);
                Log.d("SpiritLevel", String.format("TouchEvent %1.0f %1.0f", event.getX(), event.getY()));

                // Change Ref Axis
                if (Math.sqrt((xc-event.getX())*(xc-event.getX()) + (yc-event.getY())*(yc-event.getY())) > 2*r1) {
                    if (Math.abs(xc - event.getX()) < 1 * r1) RefAxe = 90;
                    if (Math.abs(yc - event.getY()) < 1 * r1) RefAxe = 0;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                break;
        }
        // Invalidate the whole view. If the view is visible.
        invalidate();
        return true;
    }


    private void DrawTextWithShadow(Canvas canvas, String text, int x, int y, float horizontal_alignment, float vertical_alignment, float rotation, Paint paint) {
        paint_Yellow_Spirit.getTextBounds(text, 0, text.length(), textbounds);
        int tHeight = textbounds.height();
        int tWidth = textbounds.width();

        canvas.save();
        canvas.rotate(rotation, x, y);
        paint_ShadowText.setAlpha(paint.getAlpha());
        canvas.drawText(text, x-tWidth*horizontal_alignment, y+tHeight*vertical_alignment, paint_ShadowText);
        canvas.drawText(text, x-tWidth*horizontal_alignment, y+tHeight*vertical_alignment, paint);
        canvas.restore();
        //canvas.drawRoundRect(rect,4, 4, paint_spirit);
    }
}