package com.example.myfirstapp.plot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.DashPathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.example.myfirstapp.plot.CarConstants;
import com.example.myfirstapp.sensors.RPISensorAdaptor;
import com.example.myfirstapp.sensors.SensorAdaptor;
import com.example.myfirstapp.sensors.SensorCoordinate;
import com.example.myfirstapp.sensors.SensorType;

import java.util.ArrayList;

public class CarPlotDemo extends View {
    private static final int SWEEP_ANGLE = 15;
    private static final float ANGLE_OFFSET = (float) Math.toDegrees(Math.atan(37/7));

    public CarPlotDemo(Context context){
        super(context);
    }
    public CarPlotDemo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CarPlotDemo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(){
        x_center = getWidth()/2;
        y_center = getHeight()/2;
        myAdaptor = RPISensorAdaptor.get_rpiadaptor(x_center, y_center);
        SensorCoordinate[] coordinates = myAdaptor.getSensors();
        left_sensors = new ArrayList<>();
        front_sensors = new ArrayList<>();
        right_sensors = new ArrayList<>();
        back_sensors = new ArrayList<>();

        for (SensorCoordinate coord : coordinates) {
            switch (coord.sensorType){
                case LEFT_FRONT:
                case RIGHT_FRONT:
                    front_sensors.add(coord);
                    break;
                case BACK:
                    back_sensors.add(coord);
                    break;
                case LEFT:
                    left_sensors.add(coord);
                    break;
                case RIGHT:
                    right_sensors.add(coord);
                    break;
                case GYRO:
                    gyro = coord;
                    break;
            }

        }
    }
    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();
    }
    private SensorAdaptor myAdaptor;
    private ArrayList<SensorCoordinate> left_sensors, front_sensors, right_sensors, back_sensors;
    private SensorCoordinate gyro;
    private int x_center, y_center;
    public boolean filled = false;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    @Override protected void onDraw(Canvas canvas) {
//        canvas.drawColor(Color.parseColor("#D3D3D3"));
        canvas.drawColor(Color.GRAY);
        super.onDraw(canvas);
        Paint paint = new Paint();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.parseColor("#FF69B4"));
        paint.setStyle(Paint.Style.FILL);

        int rectWidth = CarConstants.CAR_WIDTH;
        int rectHeight = CarConstants.CAR_LENGTH;
        int front = CarConstants.POINTY_LENGTH;

        // draw the car
        Path car = new Path();
        car.moveTo(x_center-rectWidth/2, y_center-rectHeight/2);
        car.lineTo(x_center, y_center-rectHeight/2-front);
        car.lineTo(x_center+rectWidth/2, y_center-rectHeight/2);
        car.lineTo(x_center+rectWidth/2, y_center+rectHeight/2);
        car.cubicTo(x_center+rectWidth/2, y_center+rectHeight/2, x_center,
                y_center+rectHeight/2 + 20, x_center-rectWidth/2, y_center+rectHeight/2);
        car.lineTo(x_center-rectWidth/2, y_center-rectHeight/2);
        canvas.drawPath(car, paint);

        // Draw outline on the left side of based on (x,y) of a sensor and its distance feedback
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);

        float left_end_x = -1;
        float left_end_y = -1;
        float left_prev_sensor_x = -1;
        float left_prev_sensor_y = -1;

        for (SensorCoordinate left_sensor : left_sensors) {
            float sensor_x = left_sensor.x_coord;
            float sensor_y = left_sensor.y_coord;
            float sensor_distance = left_sensor.getVal();

            float rect_left = sensor_x-sensor_distance;
            float rect_top = sensor_y-sensor_distance;
            float rect_right = sensor_x+sensor_distance;
            float rect_bottom = sensor_y+sensor_distance;
            float start_angle = (float) 172.5;

            RectF rectF = new RectF(rect_left, rect_top, rect_right, rect_bottom);

            if (filled) {
                paint.setStyle(Paint.Style.FILL);
                canvas.drawArc (rectF, start_angle, SWEEP_ANGLE, true, paint);
            }
            else {
                canvas.drawArc (rectF, start_angle, SWEEP_ANGLE, false, paint);

            }

            float half_angle = SWEEP_ANGLE/2;
            float triangle_top = sensor_distance*(float)Math.cos(Math.toRadians(half_angle));
            float triangle_bottom = triangle_top*(float)Math.cos(Math.toRadians(half_angle));
            float triangle_side = triangle_top*(float)Math.sin(Math.toRadians(half_angle));

            // Find the coordinates of the start of the curve (top)
            float curve_start_x = sensor_x - triangle_bottom;
            float curve_start_y = sensor_y - triangle_side;

            Path left_outline = new Path();

            if (left_end_x >= 0 && left_end_y >= 0 && left_prev_sensor_x >=0 && left_prev_sensor_y >= 0) {
                // fill mode
                if (filled) {
                    left_outline.moveTo(left_prev_sensor_x, left_prev_sensor_y);
                    left_outline.lineTo(left_end_x, left_end_y);
                    left_outline.lineTo(curve_start_x, curve_start_y);
                    left_outline.lineTo(sensor_x, sensor_y);
                    canvas.drawPath(left_outline, paint);
                }

                // contour mode
                else{
                    canvas.drawLine(left_end_x, left_end_y, curve_start_x, curve_start_y, paint);

                }

            }

            // Find the coordinates of the end of the curve (bottom)
            float curve_end_x = sensor_x - triangle_bottom;
            float curve_end_y = sensor_y + triangle_side;

            left_end_x = curve_end_x;
            left_end_y = curve_end_y;
            left_prev_sensor_x = sensor_x;
            left_prev_sensor_y = sensor_y;
        }


        // Draw outline on the right side of based on (x,y) of a sensor and its distance feedback

        float right_end_x = -1;
        float right_end_y = -1;
        float right_prev_sensor_x = -1;
        float right_prev_sensor_y = -1;

        for (SensorCoordinate right_sensor : right_sensors) {
            float sensor_x = right_sensor.x_coord;
            float sensor_y = right_sensor.y_coord;
            float sensor_distance = right_sensor.getVal();

            float rect_left = sensor_x-sensor_distance;
            float rect_top = sensor_y-sensor_distance;
            float rect_right = sensor_x+sensor_distance;
            float rect_bottom = sensor_y+sensor_distance;

            float start_angle = (float) -8.5;

            RectF rectF = new RectF(rect_left, rect_top, rect_right, rect_bottom);
            if (filled) {
                //filled mode
                paint.setStyle(Paint.Style.FILL);
                canvas.drawArc (rectF, start_angle, SWEEP_ANGLE, true, paint);
            }
            // contour mode
            else {
                canvas.drawArc (rectF, start_angle, SWEEP_ANGLE, false, paint);
            }


            float half_angle = SWEEP_ANGLE/2;
            float triangle_top = sensor_distance*(float)Math.cos(Math.toRadians(half_angle));
            float triangle_bottom = triangle_top*(float)Math.cos(Math.toRadians(half_angle));
            float triangle_side = triangle_top*(float)Math.sin(Math.toRadians(half_angle));

            // Find the coordinates of the start of the curve (top)
            float curve_start_x = sensor_x + triangle_bottom;
            float curve_start_y = sensor_y - triangle_side;

            Path right_outline = new Path();

            if (right_end_x >= 0 && right_end_y >= 0 && right_prev_sensor_x >=0 && right_prev_sensor_y >= 0) {
                if (filled) {
                    right_outline.moveTo(right_prev_sensor_x, right_prev_sensor_y);
                    right_outline.lineTo(right_end_x, right_end_y);
                    right_outline.lineTo(curve_start_x, curve_start_y);
                    right_outline.lineTo(sensor_x, sensor_y);
                    canvas.drawPath(right_outline, paint);
                }

                else {
                    canvas.drawLine(right_end_x, right_end_y, curve_start_x, curve_start_y, paint);
                }
            }

            // Find the coordinates of the end of the curve (bottom)
            float curve_end_x = sensor_x + triangle_bottom;
            float curve_end_y = sensor_y + triangle_side;

            right_end_x = curve_end_x-3;
            right_end_y = curve_end_y-3;
            right_prev_sensor_x = sensor_x;
            right_prev_sensor_y = sensor_y;
        }


        // Draw outline on the back of based on (x,y) of a sensor and its distance feedback

        for (SensorCoordinate back_sensor : back_sensors) {
            float sensor_x = back_sensor.x_coord;
            float sensor_y = back_sensor.y_coord;
            float sensor_distance = back_sensor.getVal();

            float rect_left = sensor_x-sensor_distance;
            float rect_top = sensor_y-sensor_distance;
            float rect_right = sensor_x+sensor_distance;
            float rect_bottom = sensor_y+sensor_distance;

            float start_angle = (float) 82.5;


            RectF rectF = new RectF(rect_left, rect_top, rect_right, rect_bottom);
            if (filled) {
                // filled mode
                paint.setStyle(Paint.Style.FILL);
                canvas.drawArc (rectF, start_angle, SWEEP_ANGLE, true, paint);
            }
            // contour mode
            else {
                canvas.drawArc (rectF, start_angle, SWEEP_ANGLE, false, paint);
            }

        }

        // Draw outline on the front of based on (x,y) of a sensor and its distance feedback

        float front_end_x = -2147483648;
        float front_end_y = -2147483648;
        float front_prev_sensor_x = -1;
        float front_prev_sensor_y = -1;

        for (SensorCoordinate front_sensor : front_sensors) {
            float sensor_x = front_sensor.x_coord;
            float sensor_y = front_sensor.y_coord;
            float sensor_distance = front_sensor.getVal();

            float rect_left = sensor_x-sensor_distance;
            float rect_top = sensor_y-sensor_distance;
            float rect_right = sensor_x+sensor_distance;
            float rect_bottom = sensor_y+sensor_distance;

            // left front sensor
            float start_angle = (float) -((90-ANGLE_OFFSET)+82.5) ;

            // right front sensor
            if (front_sensor.sensorType == SensorType.RIGHT_FRONT) {
                start_angle = (float) -(82.5-(90-ANGLE_OFFSET));
            }

            RectF rectF = new RectF(rect_left, rect_top, rect_right, rect_bottom);

            if (filled) {
                // filled mode
                paint.setStyle(Paint.Style.FILL);
                canvas.drawArc (rectF, start_angle, -SWEEP_ANGLE, true, paint);
            }
            else {
                canvas.drawArc (rectF, start_angle, -SWEEP_ANGLE, false, paint);
            }


            float half_angle = SWEEP_ANGLE/2;
            float triangle_top = sensor_distance*(float)Math.cos(Math.toRadians(half_angle));

            // LEFT FRONT SENSOR
            float straight_angle = -(start_angle)-90;
            float opp = triangle_top*(float)Math.sin(Math.toRadians(straight_angle));
            float neighbor = triangle_top*(float)Math.cos(Math.toRadians(straight_angle));

            // find the coordinates of start of the curve (left)
            float side_angle = (180-15+start_angle);
            float side = triangle_top*(float) Math.sin(Math.toRadians((side_angle)));
            float bottom = triangle_top*(float) Math.cos(Math.toRadians((side_angle)));
            float curve_start_x = sensor_x-bottom;
            float curve_start_y = sensor_y - side;

            // Find the coordinates of the end of the curve (right)
            float curve_end_x = sensor_x - opp;
            float curve_end_y = sensor_y - neighbor;

            if (front_sensor.sensorType == SensorType.RIGHT_FRONT) {
                straight_angle = (float) 90-15+start_angle;
                opp = triangle_top*(float)Math.sin(Math.toRadians(straight_angle));
                neighbor = triangle_top*(float)Math.cos(Math.toRadians(straight_angle));

                // find the coordinates of start of the curve (left)
                side_angle = -start_angle;
                side = triangle_top*(float) Math.sin(Math.toRadians((side_angle)));
                bottom = triangle_top*(float) Math.cos(Math.toRadians((side_angle)));

                curve_start_x = sensor_x + opp;
                curve_start_y = sensor_y - neighbor;

                // Find the coordinates of the end of the curve (right)
                curve_end_x = sensor_x + bottom;
                curve_end_y = sensor_y - side;
            }


            Path front_outline = new Path();

            if (front_end_x > -2147483648 && front_end_y > -2147483648) {
                if (filled) {
                    // fill mode
                    front_outline.moveTo(front_prev_sensor_x, front_prev_sensor_y);
                    front_outline.lineTo(front_end_x, front_end_y);
                    front_outline.lineTo(curve_start_x, curve_start_y);
                    front_outline.lineTo(sensor_x, sensor_y);
                    canvas.drawPath(front_outline, paint);
                }

                else {
                    canvas.drawLine(front_end_x, front_end_y, curve_start_x, curve_start_y, paint);
                }

            }

            front_end_x = curve_end_x;
            front_end_y = curve_end_y;
            front_prev_sensor_x = sensor_x;
            front_prev_sensor_y = sensor_y;
        }


        // Draw prediction
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLUE);
        paint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

        // If wheel turns, draw arcs
        float angle_ratio = ((float)-2.0)/((float)3.0);
        float wheelAngle = gyro.getVal()*angle_ratio;

        if (wheelAngle > 0) {
            double tan = Math.tan(Math.toRadians(wheelAngle));
            float turn_radius = Math.round(CarConstants.CAR_LENGTH/tan-CarConstants.CAR_WIDTH/2);
            float turn_center_x = x_center+turn_radius;
            float turn_center_y = y_center+CarConstants.CAR_LENGTH/2;

            float upper_left_x_right = turn_center_x - turn_radius + CarConstants.CAR_WIDTH/2;
            float upper_left_y_right = turn_center_y - turn_radius;
            float arcAngle_right = Math.round(2*CarConstants.CAR_LENGTH/(2*turn_radius*Math.PI)*360);

            float upper_left_x_left = turn_center_x - turn_radius -  CarConstants.CAR_WIDTH/2;
            float upper_left_y_left = turn_center_y - turn_radius - CarConstants.CAR_WIDTH;
            float arcAngle_left = Math.round(2*CarConstants.CAR_LENGTH/(2*(turn_radius+ CarConstants.CAR_WIDTH)*Math.PI)*360);

            // Draw right curve
            RectF rect_Right = new RectF(upper_left_x_right, upper_left_y_right, upper_left_x_right+2*turn_radius, upper_left_y_right+2*turn_radius);
            canvas.drawArc (rect_Right, 180, -arcAngle_right, false, paint);

            // Draw left curve
            RectF rect_Left = new RectF(upper_left_x_left, upper_left_y_left, upper_left_x_left+2*(turn_radius+ CarConstants.CAR_WIDTH), upper_left_y_left+2*(turn_radius+ CarConstants.CAR_WIDTH));
            canvas.drawArc (rect_Left, 180, -arcAngle_left, false, paint);
        }

        else if (wheelAngle < 0) {
            double tan = Math.tan(Math.toRadians(-wheelAngle));
            float turn_radius = Math.round(CarConstants.CAR_LENGTH/tan-CarConstants.CAR_WIDTH/2);
            float turn_center_x = x_center-turn_radius;
            float turn_center_y = y_center+CarConstants.CAR_LENGTH/2;

            float upper_left_x_left = turn_center_x - turn_radius - CarConstants.CAR_WIDTH/2;
            float upper_left_y_left = turn_center_y - turn_radius;
            float arcAngle_right = Math.round(2*CarConstants.CAR_LENGTH/(2*turn_radius*Math.PI)*360);

            float upper_left_x_right = turn_center_x - turn_radius -  CarConstants.CAR_WIDTH*3/2;
            float upper_left_y_right = turn_center_y - turn_radius - CarConstants.CAR_WIDTH;
            float arcAngle_left = Math.round(2*CarConstants.CAR_LENGTH/(2*(turn_radius+ CarConstants.CAR_WIDTH)*Math.PI)*360);

            // Draw right curve
            RectF rect_Left = new RectF(upper_left_x_left, upper_left_y_left, upper_left_x_left+2*turn_radius, upper_left_y_left+2*turn_radius);
            canvas.drawArc (rect_Left, 0, arcAngle_left, false, paint);

            // Draw left curve
            RectF rect_Right = new RectF(upper_left_x_right, upper_left_y_right, upper_left_x_right+2*(turn_radius+ CarConstants.CAR_WIDTH), upper_left_y_right+2*(turn_radius+ CarConstants.CAR_WIDTH));
            canvas.drawArc (rect_Right, 0, arcAngle_right, false, paint);

        }

        // If wheels didn't turn, draw straight lines
        else {
            canvas.drawLine(x_center-CarConstants.CAR_WIDTH/2, y_center+CarConstants.CAR_LENGTH/2, x_center-CarConstants.CAR_WIDTH/2, y_center+CarConstants.CAR_LENGTH*2, paint);
            canvas.drawLine(x_center+CarConstants.CAR_WIDTH/2, y_center+CarConstants.CAR_LENGTH/2, x_center+CarConstants.CAR_WIDTH/2, y_center+CarConstants.CAR_LENGTH*2, paint);
        }


    }

}
