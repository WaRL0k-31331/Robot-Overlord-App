package com.marginallyclever.robotOverlord.AHRobot;

import java.io.Serializable;

import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.Log;

/**
 * A snapshot in time of the robot in a given position.  Can run forward or inverse kinematics to calcualte the joint angles and/or the tool position.
 * @author danroyer
 *
 */
class AHRobotMotionState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012199745425607761L;
	
	public static final float EPSILON = 0.00001f;
	
	// angle of rotation
	float angleF = 0;
	float angleE = 0;
	float angleD = 0;
	float angleC = 0;
	float angleB = 0;
	float angleA = 0;
	float angleServo = 120;
	
	// robot arm coordinates.  Relative to base unless otherwise noted.
	public Vector3f fingerPosition = new Vector3f();
	public Vector3f fingerForward = new Vector3f();
	public Vector3f fingerRight = new Vector3f();
	// finger rotation, in degrees
	public float ikU=0;
	public float ikV=0;
	public float ikW=0;
	// joint locations relative to base
	Vector3f wrist = new Vector3f();
	Vector3f elbow = new Vector3f();
	Vector3f boom = new Vector3f();
	Vector3f shoulder = new Vector3f();
	
	public Vector3f anchorPosition = new Vector3f();  // relative to world
	// base orientation, affects entire arm
	public Vector3f baseForward = new Vector3f();
	public Vector3f baseUp = new Vector3f();
	public Vector3f baseRight = new Vector3f();
	
	// rotating entire robot
	float basePan=0;
	float baseTilt=0;

	// inverse kinematics visualizations
	Vector3f ikWrist = new Vector3f();
	Vector3f ikElbow = new Vector3f();
	Vector3f ikShoulder = new Vector3f();
	Vector3f ikBase = new Vector3f();
	
	
	void set(AHRobotMotionState other) {
		angleF = other.angleF;
		angleE = other.angleE;
		angleD = other.angleD;
		angleC = other.angleC;
		angleB = other.angleB;
		angleA = other.angleA;
		angleServo = other.angleServo;
		ikU=other.ikU;
		ikV=other.ikV;
		ikW=other.ikW;
		fingerForward.set(other.fingerForward);
		fingerRight.set(other.fingerRight);
		fingerPosition.set(other.fingerPosition);
		wrist.set(other.wrist);
		elbow.set(other.elbow);
		boom.set(other.boom);
		shoulder.set(other.shoulder);
		anchorPosition.set(other.anchorPosition);
		baseForward.set(other.baseForward);
		baseUp.set(other.baseUp);
		baseRight.set(other.baseRight);
		basePan = other.basePan;
		baseTilt = other.baseTilt;

		ikWrist.set(other.ikWrist);
		ikElbow.set(other.ikElbow);
		ikShoulder.set(other.ikShoulder);
		ikBase.set(other.ikBase);
	}
	
	
	// TODO check for collisions with http://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment ?
	public boolean movePermitted() {
		// don't hit floor?
		// don't hit ceiling?

		// check far limit
		// seems doable
		if(inverseKinematics()==false) return false;
		// angle are good?
		if(checkAngleLimits()==false) return false;

		// OK
		return true;
	}
	
	
	protected boolean checkAngleLimits() {/*
		// machine specific limits
		//a
		//if (angleA < -180) return false;
		//if (angleA >  180) return false;
		//b
		if (angleB <      72.90) angleB = 72.90f;
		if (angleB >  360-72.90) angleB = 360-72.90f;
		//c
		if (angleC <   50.57) angleC = 50.57f;
		if (angleC >  160.31) angleC = 160.31f;
		//d
		if (angleD <   87.85) angleD = 87.85f;
		if (angleD >  173.60) angleD = 173.60f;
		//e
		//if (angleE < 180-165) return false;
		//if (angleE > 180+165) return false;
*/
		return true;
	}
	
	
	/**
	 * Find the arm joint angles that would put the finger at the desired location.
	 * @return false if successful, true if the IK solution cannot be found.
	 */
	protected boolean inverseKinematics() {
		float n;
		double ee;
		float xx,yy;
		
		// rotation at finger, bend at wrist, rotation between wrist and elbow, then bends down to base.
		
		// find the wrist position
		Vector3f towardsFinger = new Vector3f(fingerForward);
		n = (float)AHRobot.WRIST_TO_TOOL_X;
		towardsFinger.scale(n);
		
		ikWrist = new Vector3f(fingerPosition);
		ikWrist.sub(towardsFinger);
		
		ikBase = new Vector3f(0,0,0);
		ikShoulder = new Vector3f(0,0,(float)(AHRobot.ANCHOR_ADJUST_Z + AHRobot.ANCHOR_TO_SHOULDER_Z));

		// Find the facingDirection and planeNormal vectors.
		Vector3f facingDirection = new Vector3f(ikWrist.x,ikWrist.y,0);
		if(Math.abs(ikWrist.x)<EPSILON && Math.abs(ikWrist.y)<EPSILON) {
			// Wrist is directly above shoulder, makes calculations hard.
			// TODO figure this out.  Use previous state to guess elbow?
			return false;
		}
		facingDirection.normalize();
		Vector3f up = new Vector3f(0,0,1);
		Vector3f planarRight = new Vector3f();
		planarRight.cross(facingDirection, up);
		planarRight.normalize();
		
		// Find elbow by using intersection of circles.
		// http://mathworld.wolfram.com/Circle-CircleIntersection.html
		// x = (dd-rr+RR) / (2d)
		Vector3f v0 = new Vector3f(ikWrist);
		v0.sub(ikShoulder);
		float d = v0.length();
		float R = (float)Math.abs(AHRobot.SHOULDER_TO_ELBOW);
		float r = (float)Math.abs(AHRobot.ELBOW_TO_WRIST);
		if( d > R+r ) {
			// impossibly far away
			return false;
		}
		float x = (d*d - r*r + R*R ) / (2*d);
		if( x > R ) {
			// would cause Math.sqrt(a negative number)
			return false;
		}
		v0.normalize();
		ikElbow.set(v0);
		ikElbow.scale(x);
		ikElbow.add(ikShoulder);
		// v1 is now at the intersection point between ik_wrist and ik_boom
		Vector3f v1 = new Vector3f();
		float a = (float)( Math.sqrt( R*R - x*x ) );
		v1.cross(planarRight, v0);
		v1.scale(a);
		ikElbow.add(v1);

		// angleF is the base
		// all the joint locations are now known.  find the angles.
		ee = Math.atan2(facingDirection.y, facingDirection.x);
		ee = capRotation(ee);
		angleF = 180+(float)Math.toDegrees(ee);

		// angleE is the shoulder
		Vector3f towardsElbow = new Vector3f(ikElbow);
		towardsElbow.sub(ikShoulder);
		towardsElbow.normalize();
		xx = (float)towardsElbow.z;
		yy = facingDirection.dot(towardsElbow);
		ee = Math.atan2(yy, xx);
		ee = capRotation(ee);
		angleE = 90-(float)Math.toDegrees(ee);

		// angleD is the elbow
		Vector3f towardsWrist = new Vector3f(ikWrist);
		towardsWrist.sub(ikElbow);
		towardsWrist.normalize();
		xx = (float)towardsElbow.dot(towardsWrist);
		v1.cross(planarRight,towardsElbow);
		yy = towardsWrist.dot(v1);
		ee = Math.atan2(yy, xx);
		ee = capRotation(ee);
		angleD = -(float)Math.toDegrees(ee);
		
		// angleC is the ulna rotation
		v0.set(towardsWrist);
		v0.normalize();
		v1.cross(v0,planarRight);
		v1.normalize();
		Vector3f towardsFingerAdj = new Vector3f(fingerForward);
		float tf = v0.dot(towardsFingerAdj);
		if(tf>=1-EPSILON) {
			// cannot calculate angle, leave as was
			return false;
		}
		// can calculate angle
		v0.scale(tf);
		towardsFingerAdj.sub(v0);
		towardsFingerAdj.normalize();
		xx = planarRight.dot(towardsFingerAdj);
		yy = v1.dot(towardsFingerAdj);
		ee = Math.atan2(yy, xx);
		ee = capRotation(ee);
		angleC = (float)Math.toDegrees(ee)+90;
		
		// angleB is the wrist bend
		v0.set(towardsWrist);
		v0.normalize();
		xx = v0.dot(towardsFinger);
		yy = towardsFingerAdj.dot(towardsFinger);
		ee = Math.atan2(yy, xx);
		ee = capRotation(ee);
		angleB = (float)Math.toDegrees(ee);
		
		// angleA is the hand rotation
		v0.cross(towardsFingerAdj,towardsWrist);
		v0.normalize();
		v1.cross(v0, towardsFinger);
		v1.normalize();
		
		xx = v0.dot(fingerRight);
		yy = v1.dot(fingerRight);
		ee = Math.atan2(yy, xx);
		ee = capRotation(ee);
		angleA = (float)Math.toDegrees(ee);
		
		/*
		Vector3f v0 = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		Vector3f planar = new Vector3f();
		Vector3f planeNormal = new Vector3f();
		Vector3f planeRight = new Vector3f(0,0,1);

		// Finger position is never on x=y=0 line, so this is safe.
		planar.set(fingerPosition);
		planar.z=0;
		planar.normalize();
		planeNormal.set(-planar.y,planar.x,0);
		planeNormal.normalize();
		
		// Find E
		ee = Math.atan2(planar.y, planar.x);
		ee = capRotation(ee);
		ik_angleE = (float)Math.toDegrees(ee);

		ik_shoulder.set(0,0,(float)(AHRobot.ANCHOR_ADJUST_Z+AHRobot.ANCHOR_TO_SHOULDER_Z));
		ik_boom.set((float)AHRobot.SHOULDER_TO_BOOM_X*(float)Math.cos(ee),
					(float)AHRobot.SHOULDER_TO_BOOM_X*(float)Math.sin(ee),
					(float)AHRobot.SHOULDER_TO_BOOM_Z);
		ik_boom.add(ik_shoulder);
		
		// Find wrist 
		ik_wrist.set(fingerForward);
		ik_wrist.scale(AHRobot.WRIST_TO_TOOL_X);
		ik_wrist.add(fingerPosition);
				
		// Find elbow by using intersection of circles.
		// http://mathworld.wolfram.com/Circle-CircleIntersection.html
		// x = (dd-rr+RR) / (2d)
		v0.set(ik_wrist);
		v0.sub(ik_boom);
		float d = v0.length();
		float R = (float)Math.abs(AHRobot.BOOM_TO_STICK_Y);
		float r = (float)Math.abs(AHRobot.STICK_TO_WRIST_X);
		if( d > R+r ) {
			// impossibly far away
			return false;
		}
		float x = (d*d - r*r + R*R ) / (2*d);
		if( x > R ) {
			// would cause Math.sqrt(a negative number)
			return false;
		}
		v0.normalize();
		ik_elbow.set(v0);
		ik_elbow.scale(x);
		ik_elbow.add(ik_boom);
		// v1 is now at the intersection point between ik_wrist and ik_boom
		float a = (float)( Math.sqrt( R*R - x*x ) );
		v1.cross(planeNormal, v0);
		v1.scale(-a);
		ik_elbow.add(v1);

		// find boom angle (D)
		v0.set(ik_elbow);
		v0.sub(ik_boom);
		x = -planar.dot(v0);
		float y = planeRight.dot(v0);
		dd = Math.atan2(y,x);
		dd = capRotation(dd);
		ik_angleD = (float)Math.toDegrees(dd);
		
		// find elbow angle (C)
		planar.set(v0);
		planar.normalize();
		planeRight.cross(planeNormal,v0);
		planeRight.normalize();
		v0.set(ik_wrist);
		v0.sub(ik_elbow);
		x = -planar.dot(v0);
		y = planeRight.dot(v0);
		cc = Math.atan2(y,x);
		cc = capRotation(cc);
		ik_angleC = (float)Math.toDegrees(cc);
		
		// find wrist angle (B)
		planar.set(ik_wrist);
		planar.sub(ik_elbow);
		planar.normalize();
		planeRight.cross(planeNormal,v0);
		planeRight.normalize();
		v0.set(fingerPosition);
		v0.sub(ik_wrist);
		x = -planar.dot(v0);
		y = -planeRight.dot(v0);
		bb = Math.atan2(y,x);
		bb = capRotation(bb);
		ik_angleB = (float)Math.toDegrees(bb);
		
		// find wrist rotation (A)
		v0.set(fingerPosition);
		v0.sub(ik_wrist);
		v0.normalize();
		v1.set(planeNormal);
		v2.cross(planeNormal,v0);
		v0.set(fingerRight);
		
		x = v2.dot(v0);
		y = -v1.dot(v0);
		aa = Math.atan2(y,x)-bb-Math.PI/2.0;
		aa = capRotation(aa);
		ik_angleA = (float)Math.toDegrees(aa);
		
		angleF=ik_angleF;
		angleA=ik_angleA;
		angleB=ik_angleB;
		angleC=ik_angleC;
		angleD=ik_angleD;
		angleE=ik_angleE;*/

		return true;
	}
	
	double capRotation(double aa) {
		while(aa<0        ) aa += Math.PI*2;
		while(aa>Math.PI*2) aa -= Math.PI*2;
		return aa;
	}
	
	/**
	 * Calculate the finger location from the angles at each joint
	 * @param state
	 */
	protected void forwardKinematics() {
		double f = Math.toRadians(angleF);
		double e = Math.toRadians(angleE);
		double d = Math.toRadians(180-angleD);
		double c = Math.toRadians(angleC+180);
		double b = Math.toRadians(angleB);
		double a = Math.toRadians(angleA);
		
		Vector3f originToShoulder = new Vector3f(0,0,(float)AHRobot.ANCHOR_ADJUST_Z+(float)AHRobot.ANCHOR_TO_SHOULDER_Z);
		Vector3f facingDirection = new Vector3f((float)Math.cos(f),(float)Math.sin(f),0);
		Vector3f up = new Vector3f(0,0,1);
		Vector3f planarRight = new Vector3f();
		planarRight.cross(facingDirection, up);
		planarRight.normalize();

		shoulder.set(originToShoulder);
		boom.set(originToShoulder);
		
		// boom to elbow
		Vector3f toElbow = new Vector3f(facingDirection);
		toElbow.scale( -(float)Math.cos(-e) );
		Vector3f v2 = new Vector3f(up);
		v2.scale( -(float)Math.sin(-e) );
		toElbow.add(v2);
		float n = (float)AHRobot.SHOULDER_TO_ELBOW;
		toElbow.scale(n);
		
		elbow.set(toElbow);
		elbow.add(shoulder);
		
		// elbow to wrist
		Vector3f towardsElbowOrtho = new Vector3f();
		towardsElbowOrtho.cross(toElbow, planarRight);
		towardsElbowOrtho.normalize();

		Vector3f elbowToWrist = new Vector3f(toElbow);
		elbowToWrist.normalize();
		elbowToWrist.scale( (float)Math.cos(d) );
		v2.set(towardsElbowOrtho);
		v2.scale( (float)Math.sin(d) );
		elbowToWrist.add(v2);
		n = AHRobot.ELBOW_TO_WRIST;
		elbowToWrist.scale(n);
		
		wrist.set(elbowToWrist);
		wrist.add(elbow);

		// wrist to finger
		Vector3f wristOrthoBeforeUlnaRotation = new Vector3f();
		wristOrthoBeforeUlnaRotation.cross(elbowToWrist, planarRight);
		wristOrthoBeforeUlnaRotation.normalize();
		Vector3f wristOrthoAfterRotation = new Vector3f(wristOrthoBeforeUlnaRotation);
		
		wristOrthoAfterRotation.scale( (float)Math.cos(-c) );
		v2.set(planarRight);
		v2.scale( (float)Math.sin(-c) );
		wristOrthoAfterRotation.add(v2);
		wristOrthoAfterRotation.normalize();

		Vector3f towardsFinger = new Vector3f();

		towardsFinger.set(elbowToWrist);
		towardsFinger.normalize();
		towardsFinger.scale( (float)( Math.cos(-b) ) );
		v2.set(wristOrthoAfterRotation);
		v2.scale( (float)( Math.sin(-b) ) );
		towardsFinger.add(v2);
		towardsFinger.normalize();

		fingerPosition.set(towardsFinger);
		n = (float)AHRobot.WRIST_TO_TOOL_X;
		fingerPosition.scale(n);
		fingerPosition.add(wrist);

		// finger rotation
		Vector3f v0 = new Vector3f();
		Vector3f v1 = new Vector3f();
		v0.cross(towardsFinger,wristOrthoAfterRotation);
		v0.normalize();
		v1.cross(v0,towardsFinger);
		v1.normalize();
		
		fingerRight.set(v0);
		fingerRight.scale((float)Math.cos(a));
		v2.set(v1);
		v2.scale((float)Math.sin(a));
		fingerRight.add(v2);

		fingerForward.set(towardsFinger);
		fingerForward.normalize();
	}
}