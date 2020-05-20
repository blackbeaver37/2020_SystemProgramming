import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 */
	public void openFile(String fileName) {

		 try{
	            //���� ��ü ����
	            File file = new File(fileName);
	            FileReader filereader = new FileReader(file);
	            BufferedReader bufReader = new BufferedReader(filereader);
	            
	            String line = "";
	            
	            // ���κ��� ������ �о inst�� �����ϰ� �� ���� instMap�� ����
	            while((line = bufReader.readLine()) != null){
	                Instruction inst = new Instruction(line);
	                instMap.put(inst.instname, inst);
	            }            
	            bufReader.close();
	        }catch(IOException e){
	            System.out.println(e);
	        }
	}
}

/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {

	String instname;	// ��ɾ� �̸�
	int format;		// ��ɾ� ����
	int opcode;		// ��ɾ� Opcode
	int opnum;		// �ǿ������� ��
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		
		String[] strArray = line.split("\t");
		instname = strArray[0];
		format = Integer.parseInt(strArray[1]);
		opcode = Integer.parseInt(strArray[2], 16);
		opnum = Integer.parseInt(strArray[3]);
		
	}
}
