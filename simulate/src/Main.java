package src;

import static MvCameraControlWrapper.MvCameraControl.MV_CC_EnumDevices;
import static MvCameraControlWrapper.MvCameraControlDefines.MV_GIGE_DEVICE;
import static MvCameraControlWrapper.MvCameraControlDefines.MV_OK;
import static MvCameraControlWrapper.MvCameraControlDefines.MV_USB_DEVICE;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import com.sun.jna.ptr.IntByReference;

import CPlusDll.DobotDll;
import CPlusDll.DobotDll.DobotResult;
import CPlusDll.DobotDll.EndEffectorParams;
import CPlusDll.DobotDll.JOGCommonParams;
import CPlusDll.DobotDll.JOGCoordinateParams;
import CPlusDll.DobotDll.JOGJointParams;
import CPlusDll.DobotDll.PTPCmd;
import CPlusDll.DobotDll.PTPCoordinateParams;
import CPlusDll.DobotDll.PTPJointParams;
import CPlusDll.DobotDll.PTPJumpParams;
import CPlusDll.DobotDll.Pose;

import MvCameraControlWrapper.CameraControlException;
import MvCameraControlWrapper.MvCameraControl;
import MvCameraControlWrapper.MvCameraControlDefines.Handle;
import MvCameraControlWrapper.MvCameraControlDefines.MVCC_INTVALUE;
import MvCameraControlWrapper.MvCameraControlDefines.MV_CC_DEVICE_INFO;
import MvCameraControlWrapper.MvCameraControlDefines.MV_FRAME_OUT_INFO;
import MvCameraControlWrapper.MvCameraControlDefines.MV_SAVE_IAMGE_TYPE;
import MvCameraControlWrapper.MvCameraControlDefines.MV_SAVE_IMAGE_PARAM;

//x�������170
// tip: The demo must import Jna library, inner DobotDemo folder of this project
public class Main {
    private static String ProjectPath = "C:\\Users\\Administrator\\Desktop\\Project\\Project\\simulate\\";

    //
    public static void main(String[] args) {
        try {
            Main app = new Main();
            app.Start();
            //startTestForTemp();
            startTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Main() {
        super();
    }

    static class Coordinate {
        private float x;
        private float y;

        public Coordinate(float X, float Y) {
            this.x = X;
            this.y = Y;
        }

        public void Reset(float X, float Y) {
            this.x = X;
            this.y = Y;
        }
    }

    static class GUIElement {
        private int id;//
        private Coordinate centralCoordinate;// ע�������ת��
        private String type;//
        private String textValue;
        private String semantic;
        private float width;
        private float height;
        private Boolean TestFinished;//
        private int toGUI;//

        public String toString() {
            return this.id + " ," + this.centralCoordinate.x + " ," + this.centralCoordinate.y + " ," + this.type + " ,"
                    + this.textValue + " ," + this.semantic + " ," + this.width + " ," + this.height + " ,"
                    + this.TestFinished + " ," + this.toGUI;
        }

        public GUIElement(int id, Coordinate centralCoordinate, String type) {
            this.id = id;
            this.centralCoordinate = centralCoordinate;
            this.type = type;
            this.TestFinished = false;
            this.toGUI = -1;
        }
    }

    static class traceAction {
        private int type;// 1:��� 2:�ڸ�λ�������ı���Ϣ��
        private String inputText;// ������ı���Ϣ
        private Coordinate centralCoordinate;// ����λ��
        private int presumeGUI;// ׷�ٶ���Ӧ�����ĸ�GUI�����

        public traceAction(int type, String input, Coordinate coordinate, int inGUI) {
            this.type = type;
            this.inputText = input;
            this.centralCoordinate = coordinate;
            this.presumeGUI = inGUI;
        }
    }

    static class GUI {
        private int id;// ���
        private String imageSaveLocation;//
        private String textSaveLocation;//
        private int totalElementNumber;//
        private int currentElementNumber = 0;
        private Boolean GUITestFinished = false;//
        private ArrayList<traceAction> traceArray = new ArrayList<traceAction>();//
        private ArrayList<GUIElement> elementArray = new ArrayList<GUIElement>();//

        public GUI(int id, String txtPath, String imagePath, int totalElementNumber,
                   ArrayList<GUIElement> elementArray) {
            this.id = id;
            this.textSaveLocation = txtPath;
            this.imageSaveLocation = imagePath;
            this.totalElementNumber = totalElementNumber;
            this.elementArray = elementArray;
        }
    }

    static class Constant {
        private int stopTime = 3000;
        private float phoneZ = -23;
        private float suspensionZ = 0;
        private float HomeX = 180, HomeY = -40, HomeZ = 0;
        private float SimilirityThreshold = (float) 0.8;
        private int refineStartX = 0, refineStartY = 0, refineXLength = 1820, refineYLength = 3640;
        private float scaleFactor = (float) 25.04, OriginX = 290, OriginY = 100;
        private Coordinate startSlide = new Coordinate(145, 70), endSlide = new Coordinate(180, 70);
        private Coordinate app = new Coordinate(269, 67);
        private Coordinate closeStartSlide = new Coordinate(190, 70), closeEndSlide = new Coordinate(270, 70);
    }

    static class KeyBoard {
        Coordinate[] CharacterKeyBoard = new Coordinate[30];// 1-26:A-Z,27:�ո�,28:�س�,29����л�����
        Coordinate[] NumberKeyBoard = new Coordinate[12];// 0-9:0-9,10:�س�,11����л�����
    }

    static class GUITest {
        private int totalGUINumber = 0;
        HashSet<GUI> GUISet = new HashSet<GUI>();
        ArrayList<GUI> GUIArray = new ArrayList<GUI>();
        private Boolean TestFinished = false;
        private Constant Constant = new Constant();
        private KeyBoard keyBoard = new KeyBoard();
    }

    private static GUITest appTest = new GUITest();

    private static void startTestForTemp() throws Exception {
//		ElementDetect("C:\\Users\\Administrator\\Desktop\\simulate\\GUI Image\\0.jpg");
        MoveTo(new Coordinate(170, 0), 20);
        Tap(new Coordinate(268, 67));
        MoveTo(new Coordinate(268, 67), appTest.Constant.suspensionZ);
        Tap(new Coordinate((float) 290.0, (float) 54.3));
        MoveTo(new Coordinate((float) 290.0, (float) 54.3), appTest.Constant.suspensionZ);
        Tap(new Coordinate((float) 289, (float) 39.7));
        MoveTo(new Coordinate((float) 289, (float) 39.7), appTest.Constant.suspensionZ);


    }

    // icon image switch inputbox checkbox radiobox button
    private static void startTest() throws Exception {
        System.out.println("Starting  Test!");
        MoveTo(new Coordinate(170, 0), 20);// ���ڻ�е�۹ؽ�ԭ�����ƶ����̶�λ��
        Tap(appTest.Constant.app);
        MoveTo(appTest.Constant.app, 0);
        GoHome();// ����е���ƶ���Ĭ��λ��
        Handle hCamera = ConnectCamera();// �������������Ϊ�ع�ģʽ
        String keyBoardPath = null;// ����ͼƬ���·��
        // TBD
        LoadKeyBoard(appTest.keyBoard, keyBoardPath);// ��������Ϣ����
        int i = 0;
        picture(hCamera, ProjectPath + "GUI Image\\", i + "Unrefined.jpeg");// ������ҳ��������Ϊ0.jpeg,�����GUI Image��

        String GUIPathOriginal = ProjectPath + "GUI Image\\" + i + "Unrefined.jpeg";// GUIԭʼͼƬ·��
        //System.out.println("GUIPathOri" + GUIPathOriginal);

        String GUIPath = ProjectPath + "GUI Image\\" + i + ".jpg";// GUIͼƬ·��
        //System.out.println("GUIPath" + GUIPath);

        Refine(GUIPathOriginal, GUIPath);// �ü����㵽��ͼƬ���ֻ���ͼЧ��
        //����������ijpg
        ElementDetect(GUIPath);// Ŀ�����ͼƬ���õ���Ӧ�ı�����GUIԪ�ص��ı���Ϣ
        String txtPath = ProjectPath + "ElementDetectTxtFile\\" + i + ".txt";// �ı���Ϣ·��
        // TBD
        GUI home = CreateGUI(i, txtPath, GUIPath);// �����ı���Ϣ����һ����ҳGUI
        String TempModelDir = ProjectPath + "SimilirityTempFile";// txt�ļ�ת��ģ�͵Ĵ��Ŀ¼·��
        String ModelDir = ProjectPath + "ModelImage";// ģ�Ϳ�·��
        Translate(i, txtPath);
        String TempTranslatedTxtPathForHome = ProjectPath + "SimilirityTxtTempFile\\" + i + ".txt";
        GUISimilarity(TempTranslatedTxtPathForHome, TempModelDir, ModelDir);// ����һ��ģ��
        String TempModelPathForHome = ProjectPath + "SimilirityTempFile\\" + i + ".jpg";// txt�ļ�ת��ģ�͵Ĵ��·��
        saveModel(i);// ��ŵ�GUIģ�Ϳ�
        appTest.totalGUINumber++;
        appTest.GUIArray.add(home);// ����ҳ����GUI������
        appTest.GUISet.add(home);// ����ҳ����GUI���Լ�����
        GUI nowGUI = null;// ��ǰ�ڲ��Ե�GUI
        GUI nextGUI = null;// ��һ��Ҫ���Ե�GUI
        nextGUI = home;// ������һ�����Ե�GUIΪ��ҳ
        while (!appTest.TestFinished)// ֻҪ���Ի�δ����
        {
            nowGUI = nextGUI;
            // TBD
            OneTest(nowGUI.elementArray.get(nowGUI.currentElementNumber));// �Բ��Խ���ĵ�ǰԪ�ؽ��в��ԣ����Է����������ո�Ԫ�ص�����
            GoHome();// ����е���ƶ���Ĭ��λ��

            i++;
            System.out.println("now i" + i);

            picture(hCamera, ProjectPath + "GUI Image\\", (i) + "Unrefined.jpeg");// ������
            System.out.println("����ɹ���");
            int nowI = i;
            GUIPathOriginal = ProjectPath + "GUI Image\\" + nowI + "Unrefined.jpeg";// ����������ʱ·��
            System.out.println("GUIPathOriginal" + GUIPathOriginal);

            GUIPath = ProjectPath + "GUI Image\\" + nowI + ".jpeg";
            System.out.println("GUIPath" + GUIPath);

            Refine(GUIPathOriginal, GUIPath);// ����ʱGUI�ü�
            ElementDetect(GUIPath);// ������ʱ��txt�ļ�
            String txtPathForTemp = ProjectPath + "ElementDetectTxtFile\\" + nowI + ".txt";
            Translate(nowI, txtPathForTemp);
            String TempTranslatedTxtPath = ProjectPath + "SimilirityTxtTempFile\\" + nowI + ".txt";// txt�ļ���ʱ·��
            String TempModelPath = ProjectPath + "SimilirityTempFile\\" + nowI + ".jpg";// txt�ļ�ת��ģ�͵Ĵ��·��
            Map result = GUISimilarity(TempTranslatedTxtPath, TempModelDir, ModelDir);// ��ͬ��������
            if ((int) (result.get("num")) == nowGUI.id)// �Ծ�ͣ���ڵ�ǰҳ������
            {
                Remove(GUIPath);
                Remove(TempTranslatedTxtPath);
                Remove(TempModelPath);// ɾ����ʱ����
                i--;// �����ǰ�ƶ�һλ
                nextGUI = nowGUI;// �趨��ǰҳ��Ϊ��һ�εĲ���ҳ��
            } else// ҳ����ת�����
            {
                nextGUI = appTest.GUIArray.get((int) (result.get("num")));// �趨��ת��ҳ��Ϊ��һ�β��Ե�GUI
                nowGUI.elementArray.get(nowGUI.currentElementNumber).toGUI = (int) (result.get("num"));// ����Ԫ�ر���Ϣ
                if ((float) (result.get("accuracy")) > appTest.Constant.SimilirityThreshold)// ���ƶȳ�����ֵ���������Ϊ����Ϊ��ҳ��
                {
                    Remove(GUIPath);
                    Remove(TempTranslatedTxtPath);
                    Remove(TempModelPath);// ɾ����ʱ����
                } else// ���ƶ�δ������ֵ����ΪΪ�µ�ҳ��
                {
                    saveModel(nowI);// ��ȡͬ������ģ��
                    GUI newGUI = CreateGUI(nowI, txtPathForTemp, GUIPath);// �����µ�GUI�ڵ�
                    appTest.totalGUINumber++;
                    appTest.GUIArray.add(newGUI);// ����ҳ�����GUI������
                    appTest.GUISet.add(newGUI);// ����ҳ�����GUI���Լ�����
                    // TBD
                    MaintainTrace(newGUI, nowGUI);// ������һ��GUIά���µ�GUI��׷������
                }
            }
            nowGUI.elementArray.get(nowGUI.currentElementNumber).TestFinished = true;// ��Ǹ�Ԫ�ز������
            if ((++nowGUI.currentElementNumber) == nowGUI.totalElementNumber)// �鿴Ԫ���Ƿ�Ϊ���һ��Ԫ��
            {
                nowGUI.GUITestFinished = true;// ��ǵ�ǰGUI�������
                appTest.GUISet.remove(nowGUI);// �Ӳ��Լ�����ɾ����GUI
            }
            if (nextGUI.GUITestFinished)// ��һ��GUI������ɵ����
            {
                if (!appTest.GUISet.isEmpty())// ����ҳ�漯���л���ҳ������
                {
                    nextGUI = appTest.GUISet.iterator().next();// ����ȡ����δ�������ҳ��
                    // TBD
                    Trace(nextGUI);// ׷�ٵ���ҳ��
                } else// ȫ��������ϵ����
                {
                    appTest.TestFinished = true;// ��ע������ϣ�����ѭ��
                }
            }
        }
    }

    private static void LoadKeyBoard(KeyBoard keyBoard, String keyboardPath) {

    }


    private static void Refine(String iamgePath, String resultPath) throws IOException {
        Msg("Refining Picture!");
        String originalPath = iamgePath;
        File input = new File(originalPath);
        FileInputStream fis = new FileInputStream(input);
        BufferedImage image = ImageIO.read(fis);
        int startX = appTest.Constant.refineStartX, startY = appTest.Constant.refineStartY,
                xL = appTest.Constant.refineXLength, yL = appTest.Constant.refineYLength;
        BufferedImage resultImage = new BufferedImage(xL, yL, image.getType());
        for (int x = 0; x < xL; x++) {
            for (int y = 0; y < yL; y++) {
                resultImage.setRGB(x, y, image.getRGB(x + startX, y + startY));
            }
        }
        File output = new File(resultPath);
        ImageIO.write(resultImage, "jpg", output);
        input.delete();
        Msg("Refining Picture Succeed!");
    }

    private static GUI CreateGUI(int id, String txtPath, String imagePath) throws FileNotFoundException {
        String Path = txtPath;
        File txt = new File(Path);
        System.out.println("path" + Path);
        System.out.println("bool" + txt.exists());

        Scanner scanner = new Scanner(txt);
        int elementNum = 0;
        ArrayList<GUIElement> elementArray = new ArrayList<GUIElement>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            StringTokenizer st = new StringTokenizer(line);
            int tempID = elementNum;
            Coordinate tempCentralCoordinate = null;
            String tempType = null;
            if (st.hasMoreTokens()) {
                tempCentralCoordinate = GetCoordinate(Integer.parseInt(st.nextToken()),
                        Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()),
                        Integer.parseInt(st.nextToken()));
            }
            if (st.hasMoreTokens()) {
                tempType = st.nextToken();
            }
            // ADD
            elementArray.add(new GUIElement(tempID, tempCentralCoordinate, tempType));
            elementNum++;
        }
        System.out.println("GUI create success");
        return new GUI(id, txtPath, imagePath, elementNum, elementArray);
    }

    private static Coordinate GetCoordinate(int LTX, int LTY, int RBX, int RBY) {
        return new Coordinate(appTest.Constant.OriginX - (LTY + RBY) / 2 / appTest.Constant.scaleFactor + 13, appTest.Constant.OriginY - (LTX + RBX) / 2 / appTest.Constant.scaleFactor);
    }

    private static void saveModel(int pictureNum) throws IOException {
        Msg("Saving Model!");
        File file = new File(ProjectPath + "SimilirityTempFile\\" + pictureNum + ".jpg");
        FileInputStream fis = new FileInputStream(file);
        byte[] bit = new byte[fis.available()];
        fis.read(bit);
        File file2 = new File(ProjectPath + "ModelImage\\" + pictureNum + ".jpg");
        FileOutputStream fos = new FileOutputStream(file2);
        fos.write(bit);
        fis.close();
        fos.close();
        Msg("Saving Model Succeed!");
    }

    private static void OneTest(GUIElement element) {
        switch (element.type) {
            case "InputBox":
                Tap(element.centralCoordinate);
                TypeIn(element.semantic);
                break;
            // ADD
            default:
                Tap(element.centralCoordinate);
                break;
        }
    }

    private static void Remove(String removePath) {
        File removeFile = new File(removePath);
        removeFile = new File(removePath);
        if (removeFile.exists()) {
            removeFile.delete();
        }
    }

    //	static class traceAction {
//		private int type;// 1:��� 2:�ڸ�λ�������ı���Ϣ��
//		private String inputText;// ������ı���Ϣ
//		private Coordinate centralCoordinate;// ����λ��
//		private int presumeGUI;// ׷�ٶ���Ӧ�����ĸ�GUI�����
//	}
    private static void MaintainTrace(GUI newGUI, GUI nowGUI) {
        for (int i = 0; i < nowGUI.traceArray.size(); i++) {
            newGUI.traceArray.add(nowGUI.traceArray.get(i));
        }
        for (int i = 0; i < nowGUI.totalElementNumber; i++) {
            if (i == nowGUI.currentElementNumber) continue;
            if (!nowGUI.elementArray.get(i).type.equals("Text") || !nowGUI.elementArray.get(i).type.equals("Icon") || !nowGUI.elementArray.get(i).type.equals("Image")) {
                if (nowGUI.elementArray.get(i).type.equals("InputBox")) {
                    newGUI.traceArray.add(new traceAction(2, nowGUI.elementArray.get(i).semantic, nowGUI.elementArray.get(i).centralCoordinate, nowGUI.id));
                } else {
                    newGUI.traceArray.add(new traceAction(1, null, nowGUI.elementArray.get(i).centralCoordinate, nowGUI.id));
                }
            }
        }
        if (nowGUI.elementArray.get(nowGUI.currentElementNumber).type.equals("InputBox")) {
            newGUI.traceArray.add(new traceAction(2, nowGUI.elementArray.get(nowGUI.currentElementNumber).semantic, nowGUI.elementArray.get(nowGUI.currentElementNumber).centralCoordinate, newGUI.id));
        } else {
            newGUI.traceArray.add(new traceAction(1, null, nowGUI.elementArray.get(nowGUI.currentElementNumber).centralCoordinate, newGUI.id));
        }
    }

    private static void Trace(GUI gui) {
        for (int i = 0; i < gui.traceArray.size(); i++) {
            if (gui.traceArray.get(i).type == 1) {
                Tap(gui.traceArray.get(i).centralCoordinate);
            } else {
                Tap(gui.traceArray.get(i).centralCoordinate);
                TypeIn(gui.traceArray.get(i).inputText);
            }
        }
    }

    private static void ToHomePage() {
        Slide(appTest.Constant.startSlide, appTest.Constant.endSlide);
        Slide(appTest.Constant.closeStartSlide, appTest.Constant.closeEndSlide);
        Tap(appTest.Constant.app);
    }

    private static void Translate(int id, String OriginalPath) throws Exception {
        Msg("Translating Text File!");
        DecimalFormat dataFormat = new DecimalFormat("0.0");
        String OutPutPath = ProjectPath + "SimilirityTxtTempFile\\" + id + ".txt";
        File infile = new File(OriginalPath);
        File outfile = new File(OutPutPath);
        outfile.createNewFile();
        Scanner scanner = new Scanner(infile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            StringTokenizer st = new StringTokenizer(line);
            writer.write(dataFormat.format(id) + " ");
            writer.write(dataFormat.format(Float.parseFloat(st.nextToken())) + " ");
            writer.write(dataFormat.format(Float.parseFloat(st.nextToken())) + " ");
            writer.write(dataFormat.format(Float.parseFloat(st.nextToken())) + " ");
            writer.write(dataFormat.format(Float.parseFloat(st.nextToken())) + " ");
            int type;
            switch (st.nextToken()) {
                case "Text":
                    type = 1;
                    break;
                case "Icon":
                    type = 2;
                    break;
                case "Image":
                    type = 3;
                    break;
                case "RadioButton":
                    type = 4;
                    break;
                case "CheckBox":
                    type = 5;
                    break;
                case "Switch":
                    type = 6;
                    break;
                case "InputBox":
                    type = 7;
                    break;
                default:
                    type = -1;
            }
            writer.write(dataFormat.format(type) + " " + dataFormat.format(appTest.Constant.refineXLength) + " "
                    + dataFormat.format(appTest.Constant.refineYLength) + "\n");
        }
        scanner.close();
        writer.close();
        Msg("Translating Text File Succeed!");
    }


    private static void GoHome() {
        Msg("Going Home!");
        Pose pose = new Pose();
        DobotDll.instance.GetPose(pose);
        MoveTo(new Coordinate(pose.x, pose.y), appTest.Constant.suspensionZ);
        MoveTo(new Coordinate(appTest.Constant.HomeX, appTest.Constant.HomeY), appTest.Constant.HomeZ);
        Msg("Going Home Succeed!");
    }

    private static void TypeIn(String str) {
        char[] fromString = str.toCharArray();
        int length = str.length();
        Boolean isCharacter = true;
        for (int i = 0; i < length; i++) {
            if (!Character.isDigit(fromString[i])) {
                if (isCharacter)
                    TypeCharacter(fromString[i]);
                else {
                    Tap(appTest.keyBoard.NumberKeyBoard[11]);//�������ָ������ĸ���ּ����л���λ��
                    TypeCharacter(fromString[i]);
                }
            } else {
                if (!isCharacter)
                    TypeCharacter(fromString[i]);
                else {
                    Tap(appTest.keyBoard.CharacterKeyBoard[29]);//�������ָ������ĸ���ּ����л���λ��
                    TypeCharacter(fromString[i]);
                }
            }
        }
        if (isCharacter)
            Tap(appTest.keyBoard.CharacterKeyBoard[28]);//����Ǽ��̵�������
        else
            Tap(appTest.keyBoard.CharacterKeyBoard[11]);//����Ǽ��̵�������
    }

    private static void MoveTo(Coordinate coordinate, float z) {
        IntByReference ib = new IntByReference();
        try {
            PTPCmd ptpCmd = new PTPCmd();
            ptpCmd.ptpMode = 1;
            ptpCmd.x = coordinate.x;
            ptpCmd.y = coordinate.y;
            ptpCmd.z = z;
            ptpCmd.r = 0;
            DobotDll.instance.SetPTPCmd(ptpCmd, true, ib);
            Thread.sleep(670);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void Tap(Coordinate coordinate) {
        Pose pose = new Pose();
        DobotDll.instance.GetPose(pose);
        MoveTo(new Coordinate(pose.x, pose.y), appTest.Constant.suspensionZ);
        MoveTo(coordinate, appTest.Constant.suspensionZ);
        MoveTo(coordinate, appTest.Constant.phoneZ);
    }

    private static void Slide(Coordinate startCoordinate, Coordinate endCoordinate) {
        Tap(startCoordinate);
        MoveTo(endCoordinate, appTest.Constant.phoneZ);
    }

    private static void TypeCharacter(char c) {
        if (Character.isLetter(c)) {
            int temp = c - 96;
            Tap(appTest.keyBoard.CharacterKeyBoard[temp]);
        } else if (Character.isWhitespace(c)) {
            Tap(appTest.keyBoard.CharacterKeyBoard[27]);
        }
    }

    private static void TypeNumber(char c) {
        int temp = c - 48;
        Tap(appTest.keyBoard.NumberKeyBoard[temp]);
    }

    public static void ElementDetect(String jpg_path) {
        Msg("Detecting Elements!");
        try {
            String startPath = "E:\\anaconda\\envs\\tensorflow\\python";
            String pyCommand = "C:\\Users\\Administrator\\Desktop\\Project\\Project\\simulate";
            String cd = "cmd.exe /c c: & cd " + pyCommand;
            String[] args = {"python", "yolo_video.py --image"};
            String cmd = cd + " & " + "activate tensorflow & " + args[0] + " " + args[1];
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader init = new BufferedReader(new InputStreamReader(process.getInputStream()));// ��ȡ�����������Ϣ
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));// ���������ͼƬ·��
            String line;
            while ((line = init.readLine()) != null) {
                if (line.equals("input")) {
                    String filename = jpg_path;
                    Msg(filename);
                    out.write(filename + "\r\n");
                    out.flush();
                    Thread.sleep(5000);
                } else {
                    System.out.println(line);
                }
            }
            out.close();
            init.close();
            process.waitFor();
            System.out.println("end");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Msg("Detecting Elements Succeed!");
    }

    public static Map GUISimilarity(String txt_path, String dir_path, String save_path) {
        Msg("Detecting Similar Pictures!");
        ArrayList<String> resultList = new ArrayList<>();
        String cd = "cmd.exe /c c: & cd " + save_path.substring(0, save_path.lastIndexOf("\\"));
        String[] args = {"python", "C:\\Users\\Administrator\\Desktop\\Project\\Project\\simulate\\run.py", txt_path, dir_path,
                save_path};
        try {
            String cmd = cd + " & " + "activate tensorflow & " + args[0] + " " + args[1] + " " + args[2] + " "
                    + args[3] + " " + args[4];
            System.out.println(cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            //System.out.println("hello");
            //System.out.println(process);
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            //System.out.println("hello");
            //System.out.println(reader);
            try {
                while ((line = reader.readLine()) != null) {
                    //System.out.println("hello");
                    System.out.println(line);
                    resultList.add(line);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map map = new HashMap();
        //System.out.println("hello");
        System.out.println(resultList);
        int num = Integer.parseInt(resultList.get(resultList.size() - 1).split(" ")[0]);
        float accuracy = Float.parseFloat(resultList.get(resultList.size() - 1).split(" ")[1]);
        map.put("num", num);
        map.put("accuracy", accuracy);
        Msg("Detecting Similar Pictures Succeed!");
        return map;
    }

    public static void picture(Handle hCamera, String dirName, String fileName) throws Exception {
        Msg("Taking Picture!");
        Thread.sleep(4000);
        int nRet = MV_OK;
        MVCC_INTVALUE stParam = new MVCC_INTVALUE();
        nRet = MvCameraControl.MV_CC_GetIntValue(hCamera, "PayloadSize", stParam);
        if (MV_OK != nRet) {
            System.err.printf("Get PayloadSize fail, errcode: [%#x]\n", nRet);
            return;
        }
        nRet = MvCameraControl.MV_CC_StartGrabbing(hCamera);
        if (MV_OK != nRet) {
            System.err.printf("Start Grabbing fail, errcode: [%#x]\n", nRet);
            return;
        }
        MV_FRAME_OUT_INFO stImageInfo = new MV_FRAME_OUT_INFO();
        byte[] pData = new byte[(int) stParam.curValue];
        nRet = MvCameraControl.MV_CC_GetOneFrameTimeout(hCamera, pData, stImageInfo, 1000);
        if (MV_OK != nRet) {
            System.err.printf("GetOneFrameTimeout fail, errcode:[%#x]\n", nRet);
            return;
        }
        // System.out.println("GetOneFrame: ");
        printFrameInfo(stImageInfo);
        int imageLen = stImageInfo.width * stImageInfo.height * 3;
        byte[] imageBuffer = new byte[imageLen];
        MV_SAVE_IMAGE_PARAM stSaveParam = new MV_SAVE_IMAGE_PARAM();
        stSaveParam.width = stImageInfo.width;
        stSaveParam.height = stImageInfo.height;
        stSaveParam.data = pData;
        stSaveParam.dataLen = stImageInfo.frameLen;
        stSaveParam.pixelType = stImageInfo.pixelType;
        stSaveParam.imageBuffer = imageBuffer;
        stSaveParam.imageType = MV_SAVE_IAMGE_TYPE.MV_Image_Jpeg;
        stSaveParam.methodValue = 0;
        stSaveParam.jpgQuality = 60;
        nRet = MvCameraControl.MV_CC_SaveImage(hCamera, stSaveParam);
        if (MV_OK != nRet) {
            System.err.printf("SaveImage fail, errcode: [%#x]\n", nRet);
            return;
        }
        saveDataToFile(imageBuffer, stSaveParam.imageLen, dirName, fileName);
        nRet = MvCameraControl.MV_CC_StopGrabbing(hCamera);
        if (MV_OK != nRet) {
            System.err.printf("StopGrabbing fail, errcode: [%#x]\n", nRet);
            return;
        }
        Msg("Taking Picture Succeed!");
    }

    public static Handle ConnectCamera() throws InterruptedException {
        Msg("Connecting Camera!");
        int nRet = MV_OK;
        int camIndex = -1;
        Handle hCamera = null;
        ArrayList<MV_CC_DEVICE_INFO> stDeviceList;
        // System.out.println("SDK Version " + MvCameraControl.MV_CC_GetSDKVersion());
        try {
            stDeviceList = MV_CC_EnumDevices(MV_GIGE_DEVICE | MV_USB_DEVICE);
            if (0 >= stDeviceList.size()) {
                System.out.println("No devices found!");
                return null;
            }
            int i = 0;
            for (MV_CC_DEVICE_INFO stDeviceInfo : stDeviceList) {
                // System.out.println("[camera " + (i++) + "]");
                printDeviceInfo(stDeviceInfo);
            }
        } catch (CameraControlException e) {
            System.err.println("Enumrate devices failed!" + e.toString());
            e.printStackTrace();
            return null;
        }
        camIndex = chooseCamera(stDeviceList);
        if (camIndex == -1) {
            return null;
        }
        try {
            hCamera = MvCameraControl.MV_CC_CreateHandle(stDeviceList.get(camIndex));
        } catch (CameraControlException e) {
            System.err.println("Create handle failed!" + e.toString());
            e.printStackTrace();
            hCamera = null;
            return null;
        }
        nRet = MvCameraControl.MV_CC_OpenDevice(hCamera);
        if (MV_OK != nRet) {
            System.err.printf("Connect to camera failed, errcode: [%#x]\n", nRet);
            return null;
        }
        nRet = MvCameraControl.MV_CC_SetEnumValueByString(hCamera, "TriggerMode", "Off");
        if (MV_OK != nRet) {
            System.err.printf("SetTriggerMode failed, errcode: [%#x]\n", nRet);
            return null;
        }
        nRet = MvCameraControl.MV_CC_SetEnumValueByString(hCamera, "ExposureAuto", "Continuous");
        if (MV_OK != nRet) {
            System.err.printf("SetExposureAuto failed, errcode: [%#x]\n", nRet);
            return null;
        }
        Thread.sleep(5000);
        Msg("Connecting Camera Succeed!");
        return hCamera;
    }

    public static void DisconnectCamera(Handle hCamera) {
        int nRet = MV_OK;
        if (null != hCamera) {
            nRet = MvCameraControl.MV_CC_DestroyHandle(hCamera);
            if (MV_OK != nRet) {
                System.err.printf("DestroyHandle failed, errcode: [%#x]\n", nRet);
            }
        }
    }

    // Ignore this
    private static void printDeviceInfo(MV_CC_DEVICE_INFO stDeviceInfo) {
        if (null == stDeviceInfo) {
            System.out.println("stDeviceInfo is null");
            return;
        }
        if (stDeviceInfo.transportLayerType == MV_GIGE_DEVICE) {
            // System.out.println("\tCurrentIp: " + stDeviceInfo.gigEInfo.currentIp);
            // System.out.println("\tModel: " + stDeviceInfo.gigEInfo.modelName);
            // System.out.println("\tUserDefinedName: " +
            // stDeviceInfo.gigEInfo.userDefinedName);
        } else if (stDeviceInfo.transportLayerType == MV_USB_DEVICE) {
            // System.out.println("\tUserDefinedName: " +
            // stDeviceInfo.usb3VInfo.userDefinedName);
            // System.out.println("\tSerial Number: " +
            // stDeviceInfo.usb3VInfo.serialNumber);
            // System.out.println("\tDevice Number: " +
            // stDeviceInfo.usb3VInfo.deviceNumber);
        } else {
            System.err.print("Device is not supported! \n");
        }
        // System.out.println("\tAccessible: " +
        // MvCameraControl.MV_CC_IsDeviceAccessible(stDeviceInfo, MV_ACCESS_Exclusive));
        // System.out.println("");
    }

    // Ignore this
    private static void printFrameInfo(MV_FRAME_OUT_INFO stFrameInfo) {
        if (null == stFrameInfo) {
            System.err.println("stFrameInfo is null");
            return;
        }
        StringBuilder frameInfo = new StringBuilder("");
        frameInfo.append(("\tFrameNum[" + stFrameInfo.frameNum + "]"));
        frameInfo.append("\tWidth[" + stFrameInfo.width + "]");
        frameInfo.append("\tHeight[" + stFrameInfo.height + "]");
        frameInfo.append(String.format("\tPixelType[%#x]", stFrameInfo.pixelType.getnValue()));
        // System.out.println(frameInfo.toString());
    }

    // Ignore this
    public static void saveDataToFile(byte[] dataToSave, int dataSize, String dirName, String fileName) {
        OutputStream os = null;
        try {
            File tempFile = new File(dirName);
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            os = new FileOutputStream(tempFile.getPath() + File.separator + fileName);
            os.write(dataToSave, 0, dataSize);
            // System.out.println("SaveImage succeed.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Ignore this
    public static int chooseCamera(ArrayList<MV_CC_DEVICE_INFO> stDeviceList) {
        if (null == stDeviceList) {
            return -1;
        }
        int camIndex = -1;
        while (true) {
            try {
                // System.out.print("The default camera index will be 0 \n");
                camIndex = 0;
                if ((camIndex >= 0 && camIndex < stDeviceList.size()) || -1 == camIndex) {
                    break;
                } else {
                    System.out.println("Input error: " + camIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
                camIndex = -1;
                break;
            }
        }
        if (-1 == camIndex) {
            System.out.println("Bye.");
            return camIndex;
        }
        if (0 <= camIndex && stDeviceList.size() > camIndex) {
            if (MV_GIGE_DEVICE == stDeviceList.get(camIndex).transportLayerType) {
                // System.out.println("Connect to camera[" + camIndex + "]: " +
                // stDeviceList.get(camIndex).gigEInfo.userDefinedName);
            } else if (MV_USB_DEVICE == stDeviceList.get(camIndex).transportLayerType) {
                // System.out.println("Connect to camera[" + camIndex + "]: " +
                // stDeviceList.get(camIndex).usb3VInfo.userDefinedName);
            } else {
                System.out.println("Device is not supported.");
            }
        } else {
            System.out.println("Invalid index " + camIndex);
            camIndex = -1;
        }
        return camIndex;
    }

    // Ignore this
    private void Start() {
        Msg("Connecting Dobot!");
        // System.out.printf("the number is: Start");
        DobotResult ret = DobotResult.values()[DobotDll.instance.ConnectDobot("COM5", 115200, (char) 0, (char) 0)];
        if (ret == DobotResult.DobotConnect_NotFound || ret == DobotResult.DobotConnect_Occupied) {
            System.out.printf("the number is: if");
            Msg("Connect error, code:" + ret.name());
            return;
        }
        // Msg("connect success code:" + ret.name());
        StartDobot();
        Msg("Connecting Dobot Succeed!");
        StartGetStatus();
    }

    // Ignore this
    private void StartDobot() {
        IntByReference ib = new IntByReference();
        EndEffectorParams endEffectorParams = new EndEffectorParams();
        endEffectorParams.xBias = 71.6f;
        endEffectorParams.yBias = 0;
        endEffectorParams.zBias = 0;
        DobotDll.instance.SetEndEffectorParams(endEffectorParams, false, ib);
        JOGJointParams jogJointParams = new JOGJointParams();
        for (int i = 0; i < 4; i++) {
            jogJointParams.velocity[i] = 200;
            jogJointParams.acceleration[i] = 200;
        }
        DobotDll.instance.SetJOGJointParams(jogJointParams, false, ib);
        JOGCoordinateParams jogCoordinateParams = new JOGCoordinateParams();
        for (int i = 0; i < 4; i++) {
            jogCoordinateParams.velocity[i] = 200;
            jogCoordinateParams.acceleration[i] = 200;
        }
        DobotDll.instance.SetJOGCoordinateParams(jogCoordinateParams, false, ib);
        JOGCommonParams jogCommonParams = new JOGCommonParams();
        jogCommonParams.velocityRatio = 50;
        jogCommonParams.accelerationRatio = 50;
        DobotDll.instance.SetJOGCommonParams(jogCommonParams, false, ib);
        PTPJointParams ptpJointParams = new PTPJointParams();
        for (int i = 0; i < 4; i++) {
            ptpJointParams.velocity[i] = 150;
            if (i == 3) ptpJointParams.velocity[i] = 1500;
            ptpJointParams.acceleration[i] = 150;
            if (i == 3) ptpJointParams.acceleration[i] = 1500;
        }
        DobotDll.instance.SetPTPJointParams(ptpJointParams, false, ib);
        PTPCoordinateParams ptpCoordinateParams = new PTPCoordinateParams();
        ptpCoordinateParams.xyzVelocity = 150;
        ptpCoordinateParams.xyzAcceleration = 150;
        ptpCoordinateParams.rVelocity = 150;
        ptpCoordinateParams.rAcceleration = 150;
        DobotDll.instance.SetPTPCoordinateParams(ptpCoordinateParams, false, ib);
        PTPJumpParams ptpJumpParams = new PTPJumpParams();
        ptpJumpParams.jumpHeight = 20;
        ptpJumpParams.zLimit = 180;
        DobotDll.instance.SetPTPJumpParams(ptpJumpParams, false, ib);
        DobotDll.instance.SetCmdTimeout(3000);
        DobotDll.instance.SetQueuedCmdClear();
        DobotDll.instance.SetQueuedCmdStartExec();
    }

    // Ignore this
    private void StartGetStatus() {
        Timer timerPos = new Timer();
        timerPos.schedule(new TimerTask() {
            public void run() {
                Pose pose = new Pose();
                DobotDll.instance.GetPose(pose);
                Msg("x=" + pose.x + "  " + "y=" + pose.y + "  " + "z=" + pose.z + "  " + "r=" + pose.r + "  ");
            }
        }, 1000, 5000);
    }

    // Ignore this
    private static void Msg(String string) {
        System.out.println(string);
    }

}