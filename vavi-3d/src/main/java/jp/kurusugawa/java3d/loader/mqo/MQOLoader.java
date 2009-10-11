package jp.kurusugawa.java3d.loader.mqo;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Color4b;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.LoaderBase;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.image.TextureLoader;

public class MQOLoader extends LoaderBase {
	private static class Face {
		protected final int[] mVertices;

		protected final int[] mTextureCoordinates;

		protected int mMaterialIndex;

		protected Color4b mColor;

		protected int mVertexCount;

		protected int mTextureCoordinateCount;

		private Face() {
			mVertices = new int[4];
			mTextureCoordinates = new int[4];
			mVertexCount = 0;
			mTextureCoordinateCount = 0;
		}

		public int[] getVertices() {
			int tVertexCount = mVertexCount;
			int[] tVertices = mVertices;
			int[] tResult = new int[tVertexCount];
			for (int i = 0; i < tVertexCount; i++) {
				tResult[i] = tVertices[i];
			}
			return tResult;
		}

		public int[] getVerticesByArray() {
			return getVertices();
		}

		public int[] getTextureCoordinates() {
			int tVertexCount = mTextureCoordinateCount;
			int[] tTextureCoordinates = mTextureCoordinates;
			int[] tResult = new int[tVertexCount];
			for (int i = 0; i < tVertexCount; i++) {
				tResult[i] = tTextureCoordinates[i];
			}
			return tResult;
		}

		public int[] getTextureCoordinatesByArray() {
			return getTextureCoordinates();
		}

		public int getMaterialIndex() {
			return mMaterialIndex;
		}

		public int getVertexCount() {
			return mVertexCount;
		}

		public Color4b getColor() {
			return mColor;
		}

		public boolean isTriangle() {
			return mVertexCount == 3;
		}

		public boolean isQuad() {
			return mVertexCount == 4;
		}

		public boolean hasTexture() {
			return mTextureCoordinateCount > 0;
		}

		public void addVertex(int aVertex) {
			mVertices[mVertexCount++] = aVertex;
		}

		public void addTextureCoordinate(int aTextureCoordinate) {
			mTextureCoordinates[mTextureCoordinateCount++] = aTextureCoordinate;
		}

		public void setVerticeIndices(Integer aOne, Integer aTwo, Integer aThree, Integer aFour) {
			mVertexCount = 0;
			addVertex(aFour);
			addVertex(aThree);
			addVertex(aTwo);
			addVertex(aOne);
		}

		public void setTextureCoordinateIndices(int aOne, int aTwo, int aThree, int aFour) {
			mTextureCoordinateCount = 0;
			addTextureCoordinate(aFour);
			addTextureCoordinate(aThree);
			addTextureCoordinate(aTwo);
			addTextureCoordinate(aOne);
		}

		public void setVerticeIndices(Integer aOne, Integer aTwo, Integer aThree) {
			mVertexCount = 0;
			addVertex(aThree);
			addVertex(aTwo);
			addVertex(aOne);
		}

		public void setTextureCoordinateIndices(int aOne, int aTwo, int aThree) {
			mTextureCoordinateCount = 0;
			addTextureCoordinate(aThree);
			addTextureCoordinate(aTwo);
			addTextureCoordinate(aOne);
		}

		public void setMaterialIndex(int aMaterialIndex) {
			mMaterialIndex = aMaterialIndex;
		}
	}

	private URL mBaseURL;

	public Scene load(String aFilename) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		FileReader tFileReader = new FileReader(aFilename);
		try {
			return load(tFileReader);
		} finally {
			try {
				tFileReader.close();
			} catch (IOException e) {
				// XXX close silently
			}
		}
	}

	public Scene load(URL aURL) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		try {
System.err.println("url: " + aURL);
			mBaseURL = new URL(aURL.getProtocol(), aURL.getHost(), aURL.getPort(), aURL.getPath().substring(0, aURL.getFile().length()));
			InputStream tOpenStream = aURL.openStream();
			InputStreamReader tInputStreamReader = new InputStreamReader(tOpenStream);
			return load(tInputStreamReader);
		} catch (IOException e) {
			// FIXME report stderr
			e.printStackTrace();
			throw new FileNotFoundException(aURL.toString());
		}
	}

	public Scene load(Reader aReader) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		SceneBase tSceneBase = new SceneBase();
		tSceneBase.setSceneGroup(new BranchGroup());
		try {
			TokenReader tTokenReader = new TokenReader(new BufferedReader(aReader));
			String token = tTokenReader.getLine();
			List<Appearance> tAppearances = null;
			for (token = tTokenReader.getLine(); (token = tTokenReader.getString()) != null;) {
				if ("Scene".equals(token)) {
					parseSceneChunk(tTokenReader, tSceneBase);
				} else if ("Material".equals(token)) {
					tAppearances = parseMaterialChunk(tTokenReader, tSceneBase);
				} else if ("Object".equals(token)) {
					parseObjectChunk(tTokenReader, tSceneBase, tAppearances);
				}
			}
			return tSceneBase;
		} catch (final IOException e) {
			throw new ParsingErrorException() {
				{
					super.initCause(e);
				}
			};
		}
	}

	private void parseSceneChunk(TokenReader aTokenReader, Scene scene) throws IOException {
		String token;
		while ((token = aTokenReader.getString()) != null) {
			if ("}".equals(token))
				break;
		}
	}

	private List<Appearance> parseMaterialChunk(TokenReader aTokenReader, Scene scene) throws IOException {
		List<Appearance> tAppearances = new ArrayList<Appearance>();
		int iNumberOfMaterial = aTokenReader.getInt();
		aTokenReader.next();
		for (int i = 0; i < iNumberOfMaterial; i++) {
			Appearance tAppearance = new Appearance();
			tAppearances.add(tAppearance);
			tAppearance.setUserData(aTokenReader.getString()); // TODO setName
			Color4f tColor = new Color4f();
			BufferedImage tTextureImage = null;
			BufferedImage tAlphaImage = null;
			float tAmbient = 0.2f;
			float tEmmisive = 0.0f;
			float tDiffuse = 1.0f;
			float tSpecular = 1.0f;
			float tShininess = 64f;
			while (!aTokenReader.empty()) {
				String token = aTokenReader.getString();
				if ("col".equals(token)) {
					tColor = new Color4f(aTokenReader.getFloat(), aTokenReader.getFloat(), aTokenReader.getFloat(), aTokenReader.getFloat());
				} else if ("dif".equals(token)) {
					tDiffuse = aTokenReader.getFloat();
				} else if ("amb".equals(token)) {
					tAmbient = aTokenReader.getFloat();
				} else if ("emi".equals(token)) {
					tEmmisive = aTokenReader.getFloat();
				} else if ("spc".equals(token)) {
					tSpecular = aTokenReader.getFloat();
				} else if ("power".equals(token)) {
					tShininess = aTokenReader.getFloat() / 100.0f * 127.0f + 1.0f;
				} else if ("tex".equals(token)) {
					tTextureImage = ImageIO.read(new URL(mBaseURL, aTokenReader.getString2()));
				} else if ("aplane".equals(token)) {
					tAlphaImage = ImageIO.read(new URL(mBaseURL, aTokenReader.getString2()));
				}
			}
			Material tMaterial = new Material();
			tMaterial.setAmbientColor(scaleColor(tColor, tAmbient));
			tMaterial.setEmissiveColor(scaleColor(tColor, tEmmisive));
			tMaterial.setDiffuseColor(scaleColor(tColor, tDiffuse));
			tMaterial.setSpecularColor(scaleColor(tColor, tSpecular));
			tMaterial.setShininess(tShininess);
			tMaterial.setLightingEnable(true);
			if (tTextureImage != null && tAlphaImage == null) {
				Texture tTexture = new TextureLoader(tTextureImage).getTexture();
				tAppearance.setTexture(tTexture);
			} else if (tAlphaImage != null) {
				int tHeight = tTextureImage.getHeight();
				int tWidth = tTextureImage.getWidth();
				BufferedImage tBufferedImage = new BufferedImage(tHeight, tWidth, BufferedImage.TYPE_4BYTE_ABGR);
				{
					Graphics2D tGraphics = tBufferedImage.createGraphics();
					tGraphics.drawImage(tTextureImage, null, 0, 0);
					tGraphics.dispose();
				}

				BufferedImage tAlphaGrayImage = new BufferedImage(tAlphaImage.getWidth(), tAlphaImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
				{
					Graphics2D tGraphics = tAlphaGrayImage.createGraphics();
					tGraphics.drawImage(tAlphaImage, null, 0, 0);
					tGraphics.dispose();
				}

				WritableRaster tAlphaRaster = tBufferedImage.getAlphaRaster();
				Raster tAlphaSourceRaster = tAlphaGrayImage.getRaster();
				int[] tAlphaPixcel = new int[tWidth];
				for (int y = tHeight - 1; y >= 0; y--) {
					tAlphaSourceRaster.getPixels(0, y, tWidth, 1, tAlphaPixcel);
					tAlphaRaster.setPixels(0, y, tWidth, 1, tAlphaPixcel);
				}
				Texture tTexture = new TextureLoader(tBufferedImage).getTexture();
				tAppearance.setTexture(tTexture);
			}

			tAppearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.0f - tColor.z));
			tAppearance.setMaterial(tMaterial);
		}
		return tAppearances;
	}

	private Color3f scaleColor(Color4f aBaseColor, float aScale) {
		Color3f tColor3f = new Color3f(aBaseColor.get());
		tColor3f.scale(aScale);
		return tColor3f;
	}

	private void parseObjectChunk(TokenReader aTokenReader, Scene aScene, List<Appearance> aAppearances) throws IOException {
		String tName = aTokenReader.getString2();
		String tToken = aTokenReader.getString();
		Point3f[] tVertices = null;
		do {
			if ("vertex".equals(tToken)) {
				tVertices = parseVertexChunk(aTokenReader);
			} else if ("visible".equals(tToken)) {
				// TODO
				aTokenReader.getInt();
			} else if ("color".equals(tToken)) {
				// TODO
				aTokenReader.getFloat();
				aTokenReader.getFloat();
				aTokenReader.getFloat();
			} else if ("face".equals(tToken)) {
				parseFaceChunk(aTokenReader, aScene, aAppearances, tName, tVertices);
			}
			tToken = aTokenReader.getString();
		} while (!"}".equals(tToken));
	}

	private Point3f[] parseVertexChunk(TokenReader aTokenReader) throws IOException {
		String tToken;
		Point3f[] tVertices;
		int tNumberOfVertex = aTokenReader.getInt();
		tVertices = new Point3f[tNumberOfVertex];
		tToken = aTokenReader.getString();
		if ("{".equals(tToken)) {
			for (int i = 0; i < tNumberOfVertex; i++) {
				tVertices[i] = new Point3f(aTokenReader.getFloat(), aTokenReader.getFloat(), aTokenReader.getFloat());
			}
			tToken = aTokenReader.getString();
		}
		return tVertices;
	}

	private void parseFaceChunk(TokenReader aTokenReader, Scene aScene, List<Appearance> aAppearances, String aName, Point3f[] aVertices) throws IOException {
		String tToken;
		int tNumberOfFace = aTokenReader.getInt();
		int tAppearanceCount = aAppearances.size();

		class FacePair {
			List<Face> mTriangleFaces = new ArrayList<Face>();

			List<Face> mQuadFaces = new ArrayList<Face>();
		}

		FacePair[] tMaterialFaces = new FacePair[tAppearanceCount];
		for (int i = 0; i < tAppearanceCount; i++) {
			tMaterialFaces[i] = new FacePair();
		}

		int tCurrentMaterialIndex = -1;
		List<Face> tTriangleFaces = null;
		List<Face> tQuadFaces = null;
		List<TexCoord2f> tTriangleTextureCoordinates = new HashList<TexCoord2f>();
		List<TexCoord2f> tQuadTextureCoordinates = new HashList<TexCoord2f>();

		tToken = aTokenReader.getString();
		if ("{".equals(tToken)) {
			for (int i = 0; i < tNumberOfFace; i++) {
				int tVertexCount = aTokenReader.getInt();
				Face tFace = new Face();
				while (!aTokenReader.empty()) {
					tToken = aTokenReader.getString();
					if ("V".equals(tToken)) {
						switch (tVertexCount) {
						case 3:
							tFace.setVerticeIndices(aTokenReader.getInt(), aTokenReader.getInt(), aTokenReader.getInt());
							break;
						case 4:
							tFace.setVerticeIndices(aTokenReader.getInt(), aTokenReader.getInt(), aTokenReader.getInt(), aTokenReader.getInt());
							break;
						}
					} else if ("M".equals(tToken)) {
						int tMaterialIndex = aTokenReader.getInt();
						tFace.setMaterialIndex(tMaterialIndex);
						if (tMaterialIndex == tCurrentMaterialIndex) {
							continue;
						}
						tCurrentMaterialIndex = tMaterialIndex;
						tTriangleFaces = tMaterialFaces[tMaterialIndex].mTriangleFaces;
						tQuadFaces = tMaterialFaces[tMaterialIndex].mQuadFaces;
					} else if ("UV".equals(tToken)) {
						switch (tVertexCount) {
						case 3:
							tFace.setTextureCoordinateIndices(getTextureCoordinityIndex(tTriangleTextureCoordinates, aTokenReader.getFloat(), aTokenReader.getFloat()), getTextureCoordinityIndex(tTriangleTextureCoordinates, aTokenReader.getFloat(), aTokenReader.getFloat()), getTextureCoordinityIndex(tTriangleTextureCoordinates, aTokenReader.getFloat(), aTokenReader.getFloat()));
							break;

						case 4:
							tFace.setTextureCoordinateIndices(getTextureCoordinityIndex(tQuadTextureCoordinates, aTokenReader.getFloat(), aTokenReader.getFloat()), getTextureCoordinityIndex(tQuadTextureCoordinates, aTokenReader.getFloat(), aTokenReader.getFloat()), getTextureCoordinityIndex(tQuadTextureCoordinates, aTokenReader.getFloat(), aTokenReader.getFloat()), getTextureCoordinityIndex(tQuadTextureCoordinates, aTokenReader.getFloat(), aTokenReader.getFloat()));
							break;
						}
					}
				}

				switch (tVertexCount) {
				case 3:
					tTriangleFaces.add(tFace);
					break;
				case 4:
					tQuadFaces.add(tFace);
					break;
				}
			}
		}
		tToken = aTokenReader.getString();

		for (int i = 0; i < tAppearanceCount; i++) {
			tTriangleFaces = tMaterialFaces[i].mTriangleFaces;
			tQuadFaces = tMaterialFaces[i].mQuadFaces;

			int tTriangleFaceCount = tTriangleFaces.size();
			int tQuadFaceCount = tQuadFaces.size();
			if (tTriangleFaceCount == 0 && tQuadFaceCount == 0) {
				continue;
			}

			Shape3D tShape3D = new Shape3D();
			tShape3D.setUserData(aName); // TODO setName
			tShape3D.setBoundsAutoCompute(true);
			tShape3D.setAppearance(aAppearances.get(i));

			System.out.println("------- " + aName + "------- ");
			System.out.println(aAppearances.get(i).getMaterial());

			try {
				if (tTriangleFaceCount > 0) {
					tShape3D.addGeometry(convertGeometryArray(tTriangleFaces, aVertices, tTriangleTextureCoordinates, new IndexedTriangleArray(aVertices.length, GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2, tTriangleFaceCount * 3)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (tQuadFaceCount > 0) {
					tShape3D.addGeometry(convertGeometryArray(tQuadFaces, aVertices, tQuadTextureCoordinates, new IndexedQuadArray(aVertices.length, GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2, tQuadFaceCount * 4)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			aScene.getSceneGroup().addChild(tShape3D);
		}
	}

	private int getTextureCoordinityIndex(List<TexCoord2f> aTextureCoordinates, float aU, float aV) {
		TexCoord2f tTexCoord2f = new TexCoord2f(aU, 1.0f - aV);
		int tIndexOf = aTextureCoordinates.indexOf(tTexCoord2f);
		if (tIndexOf < 0) {
			tIndexOf = aTextureCoordinates.size();
			aTextureCoordinates.add(tTexCoord2f);
		}
		return tIndexOf;
	}

	private GeometryArray convertGeometryArray(List<Face> aFaces, Point3f[] aVertices, List<TexCoord2f> aTextureCoordinates, IndexedGeometryArray aGeometryArray) {
		aGeometryArray.setCoordinates(0, aVertices);
		if (aTextureCoordinates.size() > 0) {
System.out.println("size: " + aTextureCoordinates.toArray(new TexCoord2f[0]).length);
			aGeometryArray.setTextureCoordinates(0, 0, aTextureCoordinates.toArray(new TexCoord2f[aTextureCoordinates.size()]));
		}
		int tVertexCounter = 0;
		for (Face tFace : aFaces) {
			aGeometryArray.setCoordinateIndices(tVertexCounter, tFace.getVerticesByArray());
			int tVertexIncrement = tFace.getVertexCount();
			if (tFace.hasTexture()) {
				aGeometryArray.setTextureCoordinateIndices(0, tVertexCounter, tFace.getTextureCoordinatesByArray());
			}
			tVertexCounter += tVertexIncrement;
		}

		GeometryInfo tGeometryInfo = new GeometryInfo(aGeometryArray);
		new NormalGenerator().generateNormals(tGeometryInfo);
		return tGeometryInfo.getGeometryArray();
	}

	private static class TokenReader {
		private BufferedReader mReader;

		private StringTokenizer mStringTokenizer;

		private boolean mIsInitialized;

		public TokenReader(BufferedReader bufferedReader) throws FileNotFoundException, IOException {
			mReader = bufferedReader;
			mIsInitialized = refreshTokenizer();
		}

		private boolean refreshTokenizer() throws IOException {
			String line;
			while ((line = mReader.readLine()) != null)
				if (line.length() != 0 && line.charAt(0) != '#') {
					mStringTokenizer = new StringTokenizer(line, ",; \t()", false);
					return true;
				}
			return false;
		}

		public String next() throws IOException {
			if (mIsInitialized) {
				if (mStringTokenizer.hasMoreTokens()) {
					String token = mStringTokenizer.nextToken();
					return token;
				}
				if (!(mIsInitialized = refreshTokenizer()))
					return null;
				if (mStringTokenizer.hasMoreTokens())
					return mStringTokenizer.nextToken();
			}
			return null;
		}

		public boolean empty() {
			return !mStringTokenizer.hasMoreTokens();
		}

		public float getFloat() throws IOException {
			return Float.parseFloat(next());
		}

		public int getInt() throws IOException {
			return Integer.parseInt(next());
		}

		public String getString() throws IOException {
			return next();
		}

		public String getString2() throws IOException {
			return next().replace('"', ' ').trim();
		}

		public String getLine() throws IOException {
			nextLine();
			return mReader.readLine();
		}

		public void nextLine() {
			while (mStringTokenizer.hasMoreTokens()) {
				mStringTokenizer.nextToken();
			}
		}

		public void close() throws IOException {
			mReader.close();
		}
	}

	private static class HashList<E> extends ArrayList<E> {
		private final Map<E, Integer> mIndex = new HashMap<E, Integer>();

		@Override
		public boolean add(E aE) {
			mIndex.put(aE, mIndex.size());
			return super.add(aE);
		}

		@Override
		public void add(int aIndex, E aElement) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends E> aC) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(int aIndex, Collection<? extends E> aC) {
			throw new UnsupportedOperationException();
		}

		@Override
		public E remove(int aIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> aC) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void removeRange(int aFromIndex, int aToIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(Object aO) {
			return mIndex.containsKey(aO);
		}

		@Override
		public boolean containsAll(Collection<?> aC) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int indexOf(Object aO) {
			Integer tInteger = mIndex.get(aO);
			if (tInteger == null) {
				return -1;
			}
			return tInteger.intValue();
		}
	}
}
