//import mx.events.EventDispatcher;

//[CHRIS REVIEW: Instead of using the wildcard, I broke it down to what was specifically needed with the imports
import com.blitzagency.events.EventBroadcaster;
import com.blitzagency.events.IStaticBroadcastable;

/**
 * A utility class for loading and using the Xray.
 * More information on Xray can be found here:
 * <a href="http://labs.blitzagency.com/?p=52">http://labs.blitzagency.com/?p=52</a>
 *
 * @example <pre>
 import com.blitzagency.xray.util.XrayLoader;
 var listener:Object = new Object();
 listener.xrayLoadComplete = function()
 {
 	Connector.trace("Xray has loaded...");
 }
 Connector.addEventListener("xrayLoadComplete", listener);
 Connector.loadConnector("ConnectorOnly.swf", this);
 </pre>
 *
 * @author Chris Allen	chris@cnmpro.com
 * @author John Grden 	neoRiley@gmail.com
 */
//import com.blitzagency.xray.util.Flashout;

class com.blitzagency.xray.util.XrayLoader implements IStaticBroadcastable
{
	private static var connector:MovieClip;
	private static var containerMovie:MovieClip;
	private static var componentSWFPath:String;
	private static var loaded:Boolean;
	private static var fpsMeter:Boolean;

	private static var loader:XrayLoader;
	
	private var broadcaster:EventBroadcaster;
	
	/**
	 * An event that is triggered once the xray
	 * connector componet has fully loaded and is ready to use.
	 */
	[Event("xrayLoadComplete")]

	/**
	 * An event that is triggered during the loading process of
	 * the xray connector componet
	 *
	 * @param eventObject Object that contains {percentLoaded:percentLoaded}
	 */
	 [Event("onLoadProgress")]

	/**
	 * An event that is triggered during the loading process of
	 * the xray connector componet
	 *
	 * @param eventObject Object that contains {errorCode:errorCode}
	 */
	 [Event("xrayLoadError")]

	/**
	 * Loads the Xray connector component for use with the Xray interface.
	 *
	 * @param componentSWF 	String 		The relative path to the component SWF.
	 * @param containerClip	MovieClip	The location as to where you want the
	 * 									connector loaded.(default is _root)
	 */
	 
	public static function trace()
	{
		if (loaded)
		{
			_global.tt.apply(_global.tt, arguments);
		}
	}

	/**
	 * Traces the supplied objects in the AdminTool.
	 * You can send anything you want to the trace method (objects, arrays, properties etc)
	 *
	 * tt() was created as a shortcut rather than typing out "trace".  It calls the exact same method that trace does.
	 */

	public static function tt()
	{
		if (loaded)
		{
			_global.tt.apply(_global.tt, arguments);
		}
	}

	/**
	 * tf() is used within function calls.
	 * It will output the name of the called function,
	 * the timeline in which it exists and the arguments sent.
	 *
	 * NOTE: as of 1.2.7 this is still a relatively new and untested method in the admintool.  Try sending different object references
	 * if "this" doesn't yeild any results.  Basically, it needs an object/timeline to loop to match up arguments.callee with.
	 *
	 * @param	arguments	Arguments Array		The arguments array of the function/method being called.
	 * @param	object		Object				The reference to the object that the function is apart of (movieclip/class etc).
	 *
	 * @example <pre>
	 function foo(parm1, parm2, parm3)
	 {
	 	Connector.tf(arguments, this);
	 }
	 </pre>
	 *
	 */

	public static function tf()
	{
		if (loaded)
		{
			_global.tf.traceFunction.apply(_global.tf, arguments);
		}
	}
	
	public static function loadConnector(componentSWF:String, containerClip:MovieClip, showFPS:Boolean):Void
	{
		componentSWFPath = componentSWF;
		containerMovie = !containerClip ? _root : containerClip;
		fpsMeter = showFPS;
		loadXray();
	}
	
	public static function addEventListener(eventName:String, listener:Object, methodName:String):Void 
	{
		if (loader == undefined) 
		{
			loader = new XrayLoader();	
		}
		loader.addSingletonEventListener(eventName, listener, methodName);
	}
	
	public static function removeEventListener(eventName:String, listener:Object, methodName:String):Void 
	{
		loader.removeSingletonEventListener(eventName, listener, methodName);
	}
	
	public function addSingletonEventListener(eventName:String, listener:Object, methodName:String):Void 
	{
		//[CHRIS REVIEW:  I added this check to create the new EventBroadcaster.  I also had to change 
		// the reference in EventBroadcaster from IBroadcastable to IStaticBroadcastable - that's what made it light up.
		if(broadcaster == undefined) broadcaster = new EventBroadcaster(this);
		broadcaster.addEventListener(eventName, listener, methodName);
	}

	public function broadcastSingletonEvent(eventName:String, data:Object):Void 
	{
		broadcaster.broadcastEvent(eventName, data);
	}

	public function removeSingletonEventListener(eventName:String, listener:Object, methodName:String):Void 
	{
		broadcaster.removeEventListener(eventName, listener, methodName);
	}
	
	private static function broadcastEvent(eventName:String, data:Object):Void 
	{
		if (loader == undefined) {
			loader = new XrayLoader();	
		}
		loader.broadcastSingletonEvent(eventName, data);
	}
	
	private static function loadXray():Void
	{
    	var loader:MovieClipLoader = new MovieClipLoader();
    	connector = containerMovie.createEmptyMovieClip("__xrayConnector", containerMovie.getNextHighestDepth());
    	loader.addListener(XrayLoader);
    	loader.loadClip(componentSWFPath, connector);
	}

	/**
	 * onLoadProgress() - dispatches an event everytime this method is called
	 * Create a method in your class called onLoadProgress for use with a custom preloader
	 *
	 * dispatches an object with 2 properties:
	 *
	 * @type: String - event fired ("onLoadProgress")
	 * @percentLoaded: Number - 0 thru 100 representing the downloaded amount
	 */
	private static function onLoadProgress(target_mc, loadedBytes, totalBytes)
	{
		var percentLoaded:Number = Math.floor((loadedBytes/totalBytes)*100);

		XrayLoader.broadcastEvent("onLoadProgress", {type:"onLoadProgress", percentLoaded:percentLoaded});
	}

	private static function onLoadInit(targetMC:MovieClip):Void
	{
		// initialize  connections
		
		_global.com.blitzagency.xray.Xray.initConnections();

		if(fpsMeter) _global.com.blitzagency.xray.Xray.createFPSMeter(targetMC);

		//Dispatch Event
		XrayLoader.broadcastEvent("LoadComplete", {type:"LoadComplete"});
	}

	private static function onLoadComplete(targetMC:MovieClip):Void
	{
		XrayLoader.loaded = true;
	}

	private static function onLoadError(targetMC:MovieClip, errorCode:String):Void
	{
		XrayLoader.broadcastEvent("LoadError", {type:"LoadError", errorCode:errorCode});
	}
}