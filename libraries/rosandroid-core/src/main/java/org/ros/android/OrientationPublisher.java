/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import geometry_msgs.Twist;
import geometry_msgs.TwistStamped;

import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;



/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class OrientationPublisher extends AbstractNodeMain {

  private final SensorManager sensorManager;

  private OrientationListener orientationListener;

  private final class OrientationListener implements SensorEventListener {

    private final Publisher<geometry_msgs.TwistStamped> publisher;

    private OrientationListener(Publisher<geometry_msgs.TwistStamped> publisher) {
      this.publisher = publisher;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
        float[] quaternion = new float[4];
        SensorManager.getQuaternionFromVector(quaternion, event.values);
        TwistStamped twist = publisher.newMessage();
        //twist.get.setFrameId("/map");
        // TODO(damonkohler): Should get time from the Node.
          twist.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));
          twist.getTwist().getLinear().setX(0.0);
          twist.getTwist().getLinear().setY(0.0);
          twist.getTwist().getLinear().setZ(0.0);
          twist.getTwist().getAngular().setX(quaternion[1]);
          twist.getTwist().getAngular().setY(quaternion[2]);
          twist.getTwist().getAngular().setZ(quaternion[3]);
        publisher.publish(twist);
      }
    }
  }

  public OrientationPublisher(SensorManager sensorManager) {
    this.sensorManager = sensorManager;
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("android/orientiation_sensor");
  }

  @Override
  public void onStart(ConnectedNode connectedNode) {
    try {
      Publisher<geometry_msgs.TwistStamped> publisher =
              connectedNode.newPublisher("android/orientation", "geometry_msgs/PoseStamped");
      orientationListener = new OrientationListener(publisher);
      Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
      // 10 Hz
      sensorManager.registerListener(orientationListener, sensor, 500000);
    } catch (Exception e) {
      connectedNode.getLog().fatal(e);
    }
  }
}
