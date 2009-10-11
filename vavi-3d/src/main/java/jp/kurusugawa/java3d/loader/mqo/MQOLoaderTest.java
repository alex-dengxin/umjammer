package jp.kurusugawa.java3d.loader.mqo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileNotFoundException;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.View;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.behaviors.vp.ViewPlatformBehavior;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class MQOLoaderTest extends JPanel {
	public MQOLoaderTest(String filename) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		this.mCanvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
		this.mUniverse = new SimpleUniverse(this.mCanvas3D);
		this.mSchedulingBounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);

		this.initializeUniverse(filename);
		this.initializeComponents();
	}

	public static void main(String[] args) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
	    String filename = args[0];
		JFrame tMainFrame = new JFrame("MQO View");
		tMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tMainFrame.getContentPane().setLayout(new BorderLayout());
		tMainFrame.getContentPane().add(new MQOLoaderTest(filename), BorderLayout.CENTER);
		tMainFrame.pack();
		tMainFrame.setVisible(true);
	}

	private Canvas3D mCanvas3D;

	private SimpleUniverse mUniverse;

	private BoundingSphere mSchedulingBounds;

	private BoundingSphere getSchedulingBounds() {
		return this.mSchedulingBounds;
	}

	private Canvas3D getCanvas() {
		return this.mCanvas3D;
	}

	private void initializeComponents() {
		this.setLayout(new BorderLayout());
		this.add(mCanvas3D, BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(512, 512));
	}

	private void initializeUniverse(String filename) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		this.mUniverse.addBranchGraph(this.createSceneGraph(filename));
		this.mUniverse.getViewingPlatform().setNominalViewingTransform();
		Transform3D tTransform3D = new Transform3D();
		this.mUniverse.getViewingPlatform().getViewPlatformTransform().getTransform(tTransform3D);
		System.out.println(tTransform3D);
		tTransform3D.setTranslation(new Vector3d(0d, 0d, 500d));
		System.out.println(tTransform3D);
		this.mUniverse.getViewingPlatform().getViewPlatformTransform().setTransform(tTransform3D);
		this.mUniverse.getViewingPlatform().setViewPlatformBehavior(this.createViewPlatformBehavior());
		this.mUniverse.getViewer().getView().setBackClipDistance(1000.0);
		this.mUniverse.getViewer().getView().setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);
		this.mUniverse.getViewer().getView().setDepthBufferFreezeTransparent(false);
	}

	private ViewPlatformBehavior createViewPlatformBehavior() {
		OrbitBehavior tOrbitBehavior = new OrbitBehavior(this.getCanvas(), OrbitBehavior.REVERSE_ALL);
		tOrbitBehavior.setSchedulingBounds(this.getSchedulingBounds());
		tOrbitBehavior.setZoomFactor(100d);
		tOrbitBehavior.setTransFactors(100d, 100d);
		return tOrbitBehavior;
	}

	private AmbientLight createAmbientLight() {
		AmbientLight tLight = new AmbientLight();
		tLight.setInfluencingBounds(this.getSchedulingBounds());
		return tLight;
	}

	private DirectionalLight createDirectionalLight() {
		DirectionalLight tLight = new DirectionalLight(new Color3f(1.0f, 1.0f, 1.0f), new Vector3f(-1.0f, -1.0f, 0.0f));
		tLight.setInfluencingBounds(this.getSchedulingBounds());
		return tLight;
	}

	private BranchGroup createSceneGraph(String filename) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		BranchGroup tRootObject = new BranchGroup();

		tRootObject.addChild(this.createAmbientLight());
		tRootObject.addChild(this.createDirectionalLight());
		tRootObject.addChild(new ColorCube(1d));
		// Scene tLoad = new Lw3dLoader().load(getClass().getResource("/gradriel.lwo"));
		Scene tLoad = new MQOLoader().load(getClass().getResource(filename));
		// Scene tLoad = new Loader3DS().load(getClass().getResource("/gradriel.3ds"));
		// Scene tLoad = new VrmlLoader().load(getClass().getResource("/gradriel.wrl"));
		tRootObject.addChild(tLoad.getSceneGroup());

		return tRootObject;
	}

}
