package SP20_simulator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator extends JFrame{
	ResourceManager rMgr = new ResourceManager();
	SicLoader sicLoader = new SicLoader(rMgr);
	SicSimulator sicSimulator = new SicSimulator(rMgr);
	File file = null;

	/**
	 * JTextField를 선언해둔 class
	 */
	static private class textField {
		static JTextField fileName;
		static JTextField pgName;
		static JTextField startAddrOfOP;
		static JTextField pgLength;
		static JTextField decA;
		static JTextField hexA;
		static JTextField decX;
		static JTextField hexX;
		static JTextField decL;
		static JTextField hexL;
		static JTextField decPC;
		static JTextField hexPC;
		static JTextField rSW;
		static JTextField decB;
		static JTextField hexB;
		static JTextField decS;
		static JTextField hexS;
		static JTextField decT;
		static JTextField hexT;
		static JTextField rF;
		static JTextField firstInstAddr;
		static JTextField startAddrOfM;
		static JTextField targetAddr;
		static JTextField usingMachine;
	}

	/**
	 * JList를 선언해둔 class
	 */
	static private class list {
		static JList<String> instruntion;
		static JList<String> log;
		// Instruction과 Log의 내용을 저장하는 ArrayList
		static ArrayList<String> instList = new ArrayList<>();
		static ArrayList<String> logList = new ArrayList<>();
	}

	/**
	 * ArrayList<String>을 String[]로 변환한다.
	 * ArrayList의 내용을 JList에 추가하기 위한 String[]을 만든다.
	 */
	private static String[] listToArray(ArrayList<String> arrayList){
		String[] array = new String[arrayList.size()];
		int index = 0;
		for(String str : arrayList){
			array[index] = str;
			index++;
		}
		return array;
	}
	
	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public void load(File program){
		sicLoader.load(program);
		sicSimulator.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public void oneStep(){
		sicSimulator.oneStep();
	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep(){
		while(rMgr.pgData.status != ResourceManager.STATUS_END){
			oneStep();
			update();
		}
	};
	
	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public void update(){
		// Instruction Update
		list.instList.add(rMgr.instruction);
		list.instruntion.setListData(listToArray(list.instList));
		// Log Update
		list.logList.add(rMgr.log);
		list.log.setListData(listToArray(list.logList));
		// Registers Update
		textField.decA.setText(String.format("%d", rMgr.getRegister(ResourceManager.REG_A)));
		textField.hexA.setText(String.format("%06X", rMgr.getRegister(ResourceManager.REG_A)));
		textField.decX.setText(String.format("%d", rMgr.getRegister(ResourceManager.REG_X)));
		textField.hexX.setText(String.format("%06X", rMgr.getRegister(ResourceManager.REG_X)));
		textField.decL.setText(String.format("%d", rMgr.getRegister(ResourceManager.REG_L)));
		textField.hexL.setText(String.format("%06X", rMgr.getRegister(ResourceManager.REG_L)));
		textField.decB.setText(String.format("%d", rMgr.getRegister(ResourceManager.REG_B)));
		textField.hexB.setText(String.format("%06X", rMgr.getRegister(ResourceManager.REG_B)));
		textField.decS.setText(String.format("%d", rMgr.getRegister(ResourceManager.REG_S)));
		textField.hexS.setText(String.format("%06X", rMgr.getRegister(ResourceManager.REG_S)));
		textField.decT.setText(String.format("%d", rMgr.getRegister(ResourceManager.REG_T)));
		textField.hexT.setText(String.format("%06X", rMgr.getRegister(ResourceManager.REG_T)));
		textField.rF.setText(String.format("%06X", rMgr.getRegister(ResourceManager.REG_F)));
		textField.decPC.setText(String.format("%d", rMgr.getRegister(ResourceManager.REG_PC)));
		textField.hexPC.setText(String.format("%06X", rMgr.getRegister(ResourceManager.REG_PC)));
		textField.rSW.setText(String.format("%06X", rMgr.getRegister(ResourceManager.REG_SW)));
		// Start Address in Memory Update
		textField.startAddrOfM.setText(String.format("%06X", rMgr.startAddrOfM));
		// Target Address Update
		textField.targetAddr.setText(String.format("%06X", rMgr.pgData.ta));
		// UsingMachine Update
		textField.usingMachine.setText(rMgr.pgData.recentDev);
	};

	/**
	 * 파일 Open 시 Frame 내 TextField 초기화
	 */
	public void initFrame(File file){
		// Program Info 작성
		textField.pgName.setText(rMgr.pgData.name);
		textField.startAddrOfOP.setText(String.format("%06X", rMgr.pgData.startAddr));
		textField.pgLength.setText(String.format("%X", rMgr.pgData.length));
		textField.fileName .setText(file.getName().replace(".txt", ""));
		textField.firstInstAddr.setText(String.format("%06X", rMgr.pgData.startAddr));
		textField.startAddrOfM.setText(String.format("%06X", rMgr.pgData.startAddr));
		// Registers 초기화
		textField.decA.setText("0");
		textField.hexA.setText("000000");
		textField.decX.setText("0");
		textField.hexX.setText("000000");
		textField.decL.setText("0");
		textField.hexL.setText("000000");
		textField.decB.setText("0");
		textField.hexB.setText("000000");
		textField.decS.setText("0");
		textField.hexS.setText("000000");
		textField.decT.setText("0");
		textField.hexT.setText("000000");
		textField.rF.setText("000000");
		textField.decPC.setText("0");
		textField.hexPC.setText("000000");
		textField.rSW.setText("000000");
	}

	/**
	 * GUI Frame을 작성 한다.
	 */
	public void frame(){
		// 메인 프레임 설정
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("SIC/EX Simulator");
		setBounds(700, 100, 690, 800);

		// 메뉴바 작성 (기능 미 구현)
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		JMenu aboutMenu = new JMenu("About");
		menuBar.add(aboutMenu);

		// 메인 Panel 작성
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(panel);
		panel.setLayout(null);

		// "FileName" Label, Text, Button 작성
		JLabel fileNameLabel = new JLabel("FileName :");
		fileNameLabel.setBounds(10, 10, 60, 25);
		panel.add(fileNameLabel);

		textField.fileName = new JTextField();
		textField.fileName .setColumns(10);
		textField.fileName .setBounds(80, 10, 200, 25);
		textField.fileName .setEditable(false);
		panel.add(textField.fileName );

		JButton openButton = new JButton("OPEN");
		openButton.addActionListener(arg0 -> {
			// 프로그램이 실행 중이었을 경우를 대비하여 초기화
			rMgr = new ResourceManager();
			sicLoader = new SicLoader(rMgr);
			sicSimulator = new SicSimulator(rMgr);
			file = null;
			list.logList = new ArrayList<>();
			list.instList = new ArrayList<>();
			update();

			// 파일 선택
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(".\\"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("txt", "txt"));	// 파일 필터
			fileChooser.setMultiSelectionEnabled(false);																					// 복수 선택 방지
			int ret = fileChooser.showOpenDialog(null);
			if (ret != JFileChooser.APPROVE_OPTION) {																					// 파일이 선택되지 않았다면 에러 메시지 박스 출력
				JOptionPane.showMessageDialog(null, "No File Selected", "ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// Load 및 Frame 초기화 실행
			file = fileChooser.getSelectedFile();
			load(file);
			initFrame(file);
		});
		openButton.setBounds(290, 10, 70, 25);
		panel.add(openButton);

		// "H (Header Record)" Panel 작성
		JPanel hPanel = new JPanel();
		hPanel.setBounds(10, 45, 320, 120);
		hPanel.setBorder(new TitledBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), "H (Header Record)"));
		panel.add(hPanel);
		hPanel.setLayout(null);

		JLabel hLabel_1 = new JLabel("Program Name :");
		hLabel_1.setBounds(10, 20, 120, 25);
		hPanel.add(hLabel_1);

		JLabel hLabel_2 = new JLabel("Start Address of");
		hLabel_2.setBounds(10, 45, 100, 25);
		hPanel.add(hLabel_2);


		JLabel hLabel_3 = new JLabel("Object Program :");
		hLabel_3.setBounds(15, 55, 120, 25);
		hPanel.add(hLabel_3);

		JLabel hLabel_4 = new JLabel("Length of Program :");
		hLabel_4.setBounds(10, 80, 120, 25);
		hPanel.add(hLabel_4);

		textField.pgName = new JTextField();
		textField.pgName.setColumns(10);
		textField.pgName.setBounds(140, 20, 160, 25);
		textField.pgName.setEditable(false);
		hPanel.add(textField.pgName);


		textField.startAddrOfOP = new JTextField();
		textField.startAddrOfOP.setColumns(10);
		textField.startAddrOfOP.setBounds(140, 50, 160, 25);
		textField.startAddrOfOP.setEditable(false);
		hPanel.add(textField.startAddrOfOP);

		textField.pgLength = new JTextField();
		textField.pgLength.setColumns(10);
		textField.pgLength.setBounds(140, 80, 160, 25);
		textField.pgLength.setEditable(false);
		hPanel.add(textField.pgLength);

		// "Register" Panel 작성
		JPanel rPanel_1 = new JPanel();
		rPanel_1.setBounds(10, 180, 320, 190);
		rPanel_1.setBorder(new TitledBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), "Register"));
		panel.add(rPanel_1);
		rPanel_1.setLayout(null);

		JLabel r1Label_1 = new JLabel("Dec");
		r1Label_1.setBounds(115, 10, 60, 25);
		rPanel_1.add(r1Label_1);

		JLabel r1Label_2 = new JLabel("Hex");
		r1Label_2.setBounds(235, 10, 60, 25);
		rPanel_1.add(r1Label_2);

		JLabel rLabel_A = new JLabel("A (#0)");
		rLabel_A.setBounds(10, 30, 60, 25);
		rPanel_1.add(rLabel_A);

		JLabel rLabel_X = new JLabel("X (#1)");
		rLabel_X.setBounds(10, 60, 60, 25);
		rPanel_1.add(rLabel_X);

		JLabel rLabel_L = new JLabel("L (#2)");
		rLabel_L.setBounds(10, 90, 60, 25);
		rPanel_1.add(rLabel_L);

		JLabel rLabel_PC = new JLabel("PC(#8)");
		rLabel_PC.setBounds(10, 120, 60, 25);
		rPanel_1.add(rLabel_PC);

		JLabel rLabel_SW = new JLabel("SW(#9)");
		rLabel_SW.setBounds(10, 150, 60, 25);
		rPanel_1.add(rLabel_SW);

		textField.decA = new JTextField();
		textField.decA.setColumns(10);
		textField.decA.setBounds(80, 30, 100, 25);
		textField.decA.setEditable(false);
		textField.decA.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_1.add(textField.decA);

		textField.hexA = new JTextField();
		textField.hexA.setColumns(10);
		textField.hexA.setBounds(200, 30, 100, 25);
		textField.hexA.setEditable(false);
		textField.hexA.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_1.add(textField.hexA);

		textField.decX = new JTextField();
		textField.decX.setColumns(10);
		textField.decX.setBounds(80, 60, 100, 25);
		textField.decX.setEditable(false);
		textField.decX.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_1.add(textField.decX);

		textField.hexX = new JTextField();
		textField.hexX.setColumns(10);
		textField.hexX.setBounds(200, 60, 100, 25);
		textField.hexX.setEditable(false);
		textField.hexX.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_1.add(textField.hexX);

		textField.decL = new JTextField();
		textField.decL.setColumns(10);
		textField.decL.setBounds(80, 90, 100, 25);
		textField.decL.setEditable(false);
		textField.decL.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_1.add(textField.decL);

		textField.hexL = new JTextField();
		textField.hexL.setColumns(10);
		textField.hexL.setBounds(200, 90, 100, 25);
		textField.hexL.setEditable(false);
		textField.hexL.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_1.add(textField.hexL);

		textField.decPC = new JTextField();
		textField.decPC.setColumns(10);
		textField.decPC.setBounds(80, 120, 100, 25);
		textField.decPC.setEditable(false);
		textField.decPC.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_1.add(textField.decPC);

		textField.hexPC = new JTextField();
		textField.hexPC.setColumns(10);
		textField.hexPC.setBounds(200, 120, 100, 25);
		textField.hexPC.setEditable(false);
		textField.hexPC.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_1.add(textField.hexPC);

		textField.rSW = new JTextField();
		textField.rSW.setColumns(10);
		textField.rSW.setBounds(80, 150, 220, 25);
		textField.rSW.setEditable(false);
		textField.rSW.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_1.add(textField.rSW);

		// "Register(for XE)" Panel 작성
		JPanel rPanel_2 = new JPanel();
		rPanel_2.setBounds(10, 385, 320, 160);
		rPanel_2.setBorder(new TitledBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), "Register(for XE)"));
		panel.add(rPanel_2);
		rPanel_2.setLayout(null);

		JLabel r2Label_1 = new JLabel("Dec");
		r2Label_1.setBounds(115, 10, 60, 25);
		rPanel_2.add(r2Label_1);

		JLabel r2Label_2 = new JLabel("Hex");
		r2Label_2.setBounds(235, 10, 60, 25);
		rPanel_2.add(r2Label_2);

		JLabel rLabel_B = new JLabel("B (#3)");
		rLabel_B.setBounds(10, 30, 60, 25);
		rPanel_2.add(rLabel_B);

		JLabel rLabel_S = new JLabel("S (#4)");
		rLabel_S.setBounds(10, 60, 60, 25);
		rPanel_2.add(rLabel_S);

		JLabel rLabel_T = new JLabel("T (#5)");
		rLabel_T.setBounds(10, 90, 60, 25);
		rPanel_2.add(rLabel_T);

		JLabel rLabel_F = new JLabel("F (#6)");
		rLabel_F.setBounds(10, 120, 60, 25);
		rPanel_2.add(rLabel_F);

		textField.decB = new JTextField();
		textField.decB.setColumns(10);
		textField.decB.setBounds(80, 30, 100, 25);
		textField.decB.setEditable(false);
		textField.decB.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_2.add(textField.decB);

		textField.hexB = new JTextField();
		textField.hexB.setColumns(10);
		textField.hexB.setBounds(200, 30, 100, 25);
		textField.hexB.setEditable(false);
		textField.hexB.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_2.add(textField.hexB);

		textField.decS = new JTextField();
		textField.decS.setColumns(10);
		textField.decS.setBounds(80, 60, 100, 25);
		textField.decS.setEditable(false);
		textField.decS.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_2.add(textField.decS);

		textField.hexS = new JTextField();
		textField.hexS.setColumns(10);
		textField.hexS.setBounds(200, 60, 100, 25);
		textField.hexS.setEditable(false);
		textField.hexS.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_2.add(textField.hexS);

		textField.decT = new JTextField();
		textField.decT.setColumns(10);
		textField.decT.setBounds(80, 90, 100, 25);
		textField.decT.setEditable(false);
		textField.decT.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_2.add(textField.decT);

		textField.hexT = new JTextField();
		textField.hexT.setColumns(10);
		textField.hexT.setBounds(200, 90, 100, 25);
		textField.hexT.setEditable(false);
		textField.hexT.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_2.add(textField.hexT);

		textField.rF = new JTextField();
		textField.rF.setColumns(10);
		textField.rF.setBounds(80, 120, 220, 25);
		textField.rF.setEditable(false);
		textField.rF.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel_2.add(textField.rF);

		// "E (End Record)" Panel 작성
		JPanel ePanel = new JPanel();
		ePanel.setBounds(340, 45, 320, 80);
		ePanel.setBorder(new TitledBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), "E (End Record)"));
		panel.add(ePanel);
		ePanel.setLayout(null);

		JLabel eLabel_1 = new JLabel("Address of First Instruction");
		eLabel_1.setBounds(10, 20, 180, 25);
		ePanel.add(eLabel_1);

		JLabel eLabel_2 = new JLabel("in Object Program :");
		eLabel_2.setBounds(60, 35, 150, 25);
		ePanel.add(eLabel_2);

		textField.firstInstAddr = new JTextField();
		textField.firstInstAddr.setColumns(10);
		textField.firstInstAddr.setBounds(175, 35, 130, 25);
		textField.firstInstAddr.setEditable(false);
		ePanel.add(textField.firstInstAddr);

		//  "Start Address in Memory" Label 및 Text 작성
		JLabel startAddressLabel = new JLabel("Start Address in Memory :");
		startAddressLabel.setBounds(350, 140, 175, 25);
		panel.add(startAddressLabel);

		textField.startAddrOfM = new JTextField();
		textField.startAddrOfM.setColumns(10);
		textField.startAddrOfM.setBounds(505, 140, 140, 25);
		textField.startAddrOfM.setEditable(false);
		panel.add(textField.startAddrOfM);

		// "Target Address" Label 및 Text 작성
		JLabel targetAddressLabel = new JLabel("Target Address :");
		targetAddressLabel.setBounds(350, 170, 175, 25);
		panel.add(targetAddressLabel);

		textField.targetAddr = new JTextField();
		textField.targetAddr.setColumns(10);
		textField.targetAddr.setBounds(505, 170, 140, 25);
		textField.targetAddr.setEditable(false);
		panel.add(textField.targetAddr);

		// "Instruction" Label 및 Text 작성
		JLabel instructionLabel = new JLabel("Instructions :");
		instructionLabel.setBounds(350, 200, 175, 25);
		panel.add(instructionLabel);

		JScrollPane instPane = new JScrollPane();
		instPane.setBounds(350, 230, 140, 310);
		list.instruntion = new JList();
		list.instruntion.setBounds(350, 230, 140, 310);
		instPane.setViewportView(list.instruntion);
		panel.add(instPane);


		// "Using Machine" Label 및 Text 작성
		JLabel usingMachineLabel = new JLabel("사용중인 장치");
		usingMachineLabel.setBounds(505, 250, 175, 25);
		panel.add(usingMachineLabel);

		textField.usingMachine = new JTextField();
		textField.usingMachine.setColumns(10);
		textField.usingMachine.setBounds(505, 270, 140, 25);
		textField.usingMachine.setEditable(false);
		panel.add(textField.usingMachine);

		// 실행 및 종료버튼 작성
		JButton excuteOneButton = new JButton("실행 (1 Step)");
		excuteOneButton.addActionListener(e -> {
			if (rMgr.pgData.status != ResourceManager.STATUS_END) {
				oneStep();
				update();
			}
		});
		excuteOneButton.setBounds(505, 420, 140, 25);
		panel.add(excuteOneButton);

		JButton excuteAllButton = new JButton("실행 (All)");
		excuteAllButton.addActionListener(e -> {
			if (rMgr.pgData.status != ResourceManager.STATUS_END) {
				allStep();
			}
		});
		excuteAllButton.setBounds(505, 460, 140, 25);
		panel.add(excuteAllButton);

		JButton endButton = new JButton("종료");
		endButton.addActionListener(e -> System.exit(0));
		endButton.setBounds(505, 500, 140, 25);
		panel.add(endButton);

		//  "Log (About Instruction)" Label 및 Text 작성
		JLabel logLabel = new JLabel("Log (About Instruction) :");
		logLabel.setBounds(15, 555, 180, 25);
		panel.add(logLabel);

		JScrollPane logPane = new JScrollPane();
		logPane.setBounds(15, 585, 645, 140);
		list.log = new JList();
		list.log.setBounds(15, 585, 645, 140);
		logPane.setViewportView(list.log);
		panel.add(logPane);
	}

	public static void main(String[] args) {
		VisualSimulator vs = new VisualSimulator();
		vs.frame();
		vs.setVisible(true);
	}
}
