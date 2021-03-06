package MyRobot;

import robocode.*;

import java.awt.Color;

/**

 * SnippetBot - a robot by Alisdair Owens

 * This bot includes all sorts of useful snippets.  It is not

 * designed to be a good fighter (although it does well 1v1),

 * just to show how certain things are done

 * Bits of code lifted from Nicator and Chrisbot

 * Conventions in this bot include: Use of radians throughout

 * Storing absolute positions of enemy bots rather than relative ones

 * Very little code in events

 * These are all good programming practices for robocode

 * There may also be methods that arent used; these might just be useful for you.

 */

public class SnippetBot extends AdvancedRobot

{

    /**

     * run: SnippetBot's default behavior

     */

    Enemy target;                   //our current enemy  代表对手，包括了对手的所有有用参数

    final double PI = Math.PI;          //just a constant 

    int direction = 1;              //direction we are heading...1 = forward, -1 = backwards

                                    //我们坦克车头的方向

    double firePower;                   //the power of the shot we will be using - set by do firePower() 设置我们的火力

 

    public void run() 

    {

        target = new Enemy();               //实例化Enemy()类

        target.distance = 100000;           //initialise the distance so that we can select a target

        setColors(Color.red,Color.blue,Color.green);    //sets the colours of the robot

        //the next two lines mean that the turns of the robot, gun and radar are independant

        //让gun,radar独立于坦克车
        setAdjustGunRobot(true);
        setAdjustRadarRobot(true);
        setAdjustRadarForGunTurn(true);

        turnRadarRightRadians(2*PI);            //turns the radar right around to get a view of the field 以弧度计算旋转一周

        

        while(true) 

        {

            doMovement();               //Move the bot 移动机器人

            doFirePower();              //select the fire power to use 选择火力

            doScanner();                //Oscillate the scanner over the bot 扫描

            doGun();                    //move the gun to predict where the enemy will be 预测敌人，调整炮管

            out.println(target.distance);       

            setFire(firePower);            //所有动作完成后，开火

            execute();              //execute all commands  上面使用的都为AdvancedRobot类中的非阻塞调用

                                    //控制权在我们，所有这里用阻塞方法返回控制给机器人

        }

    }


    /*

      * This simple function calculates the fire power to use (from 0.1 to 3)

      * based on the distance from the target.  We will investigate the data structure

      * holding the target data later.

      */

    void doFirePower() 

    {

        firePower = 400/target.distance;//selects a bullet power based on our distance away from the target 

                                        //根据敌人距离来选择火力,因为本身前进，后退为300，所以火力不会过大

    }


    /*

      * This is the movememnt function.  It will cause us

      * to circle strafe the enemy (ie move back and forward,

      * circling the enemy.  if you don't know what strafing means

      * play more quake.

      * The direction variable is global to the class.  Passing a

      * negative number to setAhead causes the bot to go backwards

      * 以目标主中心来回摆动

      */

    void doMovement() 

    {

        if (getTime()%20 == 0)  //?过20的倍数时间就反转方向

        {

            //every twenty 'ticks'

            direction *= -1;        //reverse direction

            setAhead(direction*300);    //move in that direction

        }

        setTurnRightRadians(target.bearing + (PI/2)); //every turn move to circle strafe the enemy 

                                                      //每一时间周期以敌人为中心绕圆运动

    }

    /*

    * this scanner method allows us to make our scanner track our target.

    * it will track to where our target is at the moment, and some further

    * in case the target has moved.  This way we always get up to the minute

    * information on our target   雷达锁定目标

    */

    void doScanner() 

    {

        double radarOffset;  //雷达偏移量

        if (getTime() - target.ctime > 4) //???why来回扫了4个回合都没扫到意味失去了目标，再全扫一遍

        {

            //if we haven't seen anybody for a bit....

            radarOffset = 360;      //rotate the radar to find a target

        } 

        else 

        {

            //next is the amount we need to rotate the radar by to scan where the target is now

            //通过扫描决定雷达旋转的弧度，"见基本原理方向剖析及目标锁定www.robochina.org".雷达弧度-敌人角度得到两者相差为旋转值

            radarOffset = getRadarHeadingRadians() - absbearing(getBFH(),getBFH(),target.x,target.y);

            //this adds or subtracts small amounts from the bearing for the radar to produce the wobbling

            //and make sure we don't lose the target

            //在得到的角度中加或减一点角度，让雷达很小的范围内摆而不失去目标

            if (radarOffset < 0)

            radarOffset -= PI/8;  //(0.375)

            else

            radarOffset += PI/8; 

        }

        //turn the radar

        setTurnRadarLeftRadians(NormaliseBearing(radarOffset)); //左转调整转动角度到PI内

    }

    /*

    * This simple method moves the gun to the bearing that we predict the

    * enemy will be by the time our bullet will get there.

    * the 'absbearing' method can be found in the helper functions section

    * the nextX and nextY method can be found in the 'Enemy' class description

    */

    void doGun() 

    {

        //works out how long it would take a bullet to travel to where the enemy is *now*

        //this is the best estimation we have 

        //计算子弹到达目标的时间长speed = 20 - 3 * power;有计算公式,距离除速度=时间

        long time = getTime() + (int)(target.distance/(20-(3*firePower)));

        //offsets the gun by the angle to the next shot based on linear targeting provided by the enemy class

        //以直线为目标，偏移子弹下一次发射的角度。（这样让子弹射空的几率减少。但对付不动的和做圆运动的机器人有问题）

        //target.guesssX(),target.guessY()为目标移动后的坐标

        double gunOffset = getGunHeadingRadians() - absbearing(getBFH(),getBFH(),target.x,target.y);

        setTurnGunLeftRadians(NormaliseBearing(gunOffset));  //调整相对角度到2PI内

    }

    /*

    * This set of helper methods.  You may find several of these very useful

    * They include the ability to find the angle to a point.

    */

    //if a bearing is not within the -pi to pi range, alters it to provide the shortest angle

    double NormaliseBearing(double ang) 

    {

        if (ang > PI)

        ang -= 2*PI;

        if (ang < -PI)

        ang += 2*PI;

        return ang;

    }

    //if a heading is not within the 0 to 2pi range, alters it to provide the shortest angle

    double NormaliseHeading(double ang) 

    {

        if (ang > 2*PI)

        ang -= 2*PI;

        if (ang < 0)

        ang += 2*PI;

        return ang;

    }

    //returns the distance between two x,y coordinates '**'

    //以两边长求得与对手之间的距离

    public double getrange( double x1,double y1, double x2,double y2 )

    {

        double xo = x2-x1;

        double yo = y2-y1;

        double h = Math.sqrt( xo*xo + yo*yo ); 

        return h;   

    }

    //gets the absolute bearing between to x,y coordinates

    //根据x,y的坐标求出绝对角度，见"坐标锁定"利用直角坐标系来反求出角度。？？？

    public double absbearing( double x1,double y1, double x2,double y2 )

    {

        double xo = x2-x1;

        double yo = y2-y1;

        double h = getrange( x1,y1, x2,y2 );

        if( xo > 0 && yo > 0 )

        {

            //反正弦定义，对边除斜边得弧度.以robocode中的绝对方向系及坐标系参照

            //x,y为正右上角为0-90,x正y负右下角为90-180,x,y负左下角180-270,x负，y正右上角270-360

            //此处要理解robocode中的绝对角度是上为0,下为180，如以中心为点划分象限则得到下面的结果

            return Math.asin( xo / h );

        }

        if( xo > 0 && yo < 0 )

        {

            return Math.PI - Math.asin( xo / h ); //x为正,y为负第二象限角

        }

        if( xo < 0 && yo < 0 )

        {

            return Math.PI + Math.asin( -xo / h ); //第三象限内180+角度

        }

        if( xo < 0 && yo > 0 )

        {

            return 2.0*Math.PI - Math.asin( -xo / h ); //四象限360-角度

        }

        return 0;

    }

    /**

    * onScannedRobot: What to do when you see another robot

     * 扫描事件,也是初始化目标数据的过程

    */

    public void onScannedRobot(ScannedRobotEvent e) 

    {

        //if we have found a closer robot....

        if ((e.getDistance() < target.distance)||(target.name == e.getName())) 

        {

            //the next line gets the absolute bearing to the point where the bot is

            //求得对手的绝对弧度

            double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*PI);

            //this section sets all the information about our target

            target.name = e.getName();

            //求得对手的x,y坐标，见"robocode基本原理之坐标锁定"文章

            target.x = getBFH()+Math.sin(absbearing_rad)*e.getDistance(); //works out the x coordinate of where the target is

            target.y = getBFH()+Math.cos(absbearing_rad)*e.getDistance(); //works out the y coordinate of where the target is

            target.bearing = e.getBearingRadians();

            target.head = e.getHeadingRadians();

            target.ctime = getTime();               //game time at which this scan was produced 扫描到机器人的游戏时间

            target.speed = e.getVelocity();         //得到敌人速度

            target.distance = e.getDistance();

        }

    }

    public void onRobotDeath(RobotDeathEvent e) 

    {

        if (e.getName() == target.name)

        target.distance = 10000; //this will effectively make it search for a new target

    }   

 

}

 

 

/*

* This class holds scan data so that we can remember where enemies were

* and what they were doing when we last scanned then.

* You could make a hashtable (with the name of the enemy bot as key)

* or a vector of these so that you can remember where all of your enemies are

* in relation to you.

* This class also holds the guessX and guessY methods. These return where our targeting

* system thinks they will be if they travel in a straight line at the same speed

* as they are travelling now.  You just need to pass the time at which you want to know

* where they will be.

* 保存我们扫描到的目标的所有有用数据，也可用hashtable，vector方法处理所有和我们有关的目标数据(用于群战)

* 中间的guessX,guessY方法是针对做直线均速运动机器人一个策略

*/

class Enemy 

{

    /*

     * ok, we should really be using accessors and mutators here,

     * (i.e getName() and setName()) but life's too short.

     */

    String name;

    public double bearing;

    public double head;

    public long ctime; //game time that the scan was produced

    public double speed;

    public double x,y;

    public double distance;

//    public double guessX(long when)
//
//    {
//
//        //以扫描时和子弹到达的时间差 ＊ 最大速度=距离, 再用对手的坐标加上移动坐标得到敌人移动后的坐标
//
//        long diff = when - ctime;
//
//        return x+Math.sin(head)*speed*diff; //目标移动后的坐标
//
//    }
//
//    public double guessY(long when)
//
//    {
//
//        long diff = when - ctime;
//
//        return y+Math.cos(head)*speed*diff;
//
//    }

}