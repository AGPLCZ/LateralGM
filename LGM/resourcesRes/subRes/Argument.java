package resourcesRes.subRes;

import resourcesRes.ResId;

public class Argument
{
    public static final byte ARG_EXPRESSION=0;
    public static final byte ARG_STRING=1;
    public static final byte ARG_BOTH=2;
    public static final byte ARG_BOOLEAN=3;
    public static final byte ARG_MENU=4;
    public static final byte ARG_COLOR=13;
    @Deprecated
    public static final byte ARG_FONTSTRING=15;
    public static final byte ARG_SPRITE=5;
    public static final byte ARG_SOUND=6;
    public static final byte ARG_BACKGROUND=7;
    public static final byte ARG_PATH=8;
    public static final byte ARG_SCRIPT=9;
    public static final byte ARG_GMOBJECT=10;
    public static final byte ARG_ROOM=11;
    public static final byte ARG_FONT=12;
    public static final byte ARG_TIMELINE=14;
    
    public byte Kind=ARG_EXPRESSION;
    public String Val="";
    public ResId Res=null;//for references to Resources
}