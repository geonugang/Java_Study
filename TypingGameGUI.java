import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.text.*;

class TypingGameGUI extends JFrame {
    private String[] sentences;
    private Random random;
    private JTextPane textPane;
    private JLabel promptLabel;
    private JLabel timeLabel;
    private JLabel accuracyLabel;
    private JButton startButton;
    private long startTime;
    private int sentenceIndex;
    private boolean[] sentenceUsed;
    private double totalElapsedTime;
    private double totalAccuracy;
    private int sentencesCount;

    public TypingGameGUI(String[] sentences) {
        this.sentences = sentences;
        this.random = new Random();
        this.sentenceUsed = new boolean[sentences.length];
        this.totalElapsedTime = 0;
        this.totalAccuracy = 0;
        this.sentencesCount = 0;

        setTitle("타자 연습 게임");                     // 프레임에 제목 붙이기
        setSize(500, 150);             // 프레임 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 프레임의 닫기 버튼 (X 버튼)을 클릭할 때 종료
        setLocationRelativeTo(null);                 // 프레임의 위치를 화면의 중앙으로 설정

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());  //BorderLayout 은 프레임을 5개의 영역(북, 남, 동, 서, 중앙)으로 나누어 구성 요소를 배치

        promptLabel = new JLabel("게임을 시작하려면 '시작' 버튼을 클릭하세요.", SwingConstants.CENTER);
        promptLabel.setFont(new Font("Serif", Font.BOLD, 20));
        add(promptLabel, BorderLayout.NORTH);
        /*
        JLabel 객체를 생성하고 초기 텍스트설정
        SwingConstants.CENTER 를 사용하여 텍스트를 중앙에 정렬
        promptLabel 의 폰트를 "Serif", 굵게, 크기 20으로 설정
        BorderLayout.NORTH 을 통해 promptLabel 을 북쪽(윗쪽상단)에 추가
         */

        textPane = new JTextPane();  //JTextPane 은 여러 스타일의 텍스트를 포함할 수 있는 텍스트 컴포넌트
        textPane.setEnabled(false);  //텍스트 입력이 불가능하도록 설정한 후 게임 시작 시 활성화 되도록 함
        textPane.setFont(new Font("Serif", Font.PLAIN, 18)); //텍스트를 입력할 때 사용할 폰트를 지정
        textPane.setPreferredSize(new Dimension(300, 100)); //textPane 의 크기를 높이 넓이 300, 높이 100으로 지정
        textPane.addKeyListener(new KeyAdapter() {    //KeyAdapter 를 사용하여 특정 키 이벤트를 처리 KeyAdapter 는 KeyListener 인터페이스를 구현
            @Override
            public void keyReleased(KeyEvent e) {             //키가 릴리즈될 때 호출되는 메서드를 오버라이드
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    checkResult();   //checkResult 메서드를 호출하여 결과를 확인
                } else {
                    highlightText(); //사용자가 입력한 텍스트와 주어진 텍스트를 비교하여 틀린 부분을 빨간색으로 표시
                }
            }
        });
        add(new JScrollPane(textPane), BorderLayout.CENTER); // JScrollPane 객체를 생성하고, 이를 textPane 으로 초기화
                                                            // BorderLayout.CENTER 을 이용하여 중앙에 배치

        JPanel infoPanel = new JPanel(new GridLayout(1, 3)); // infoPanel 이라는 이름으로 JPanel 객체를 생성하고 GridLayout 을 사용하여 레이아웃을 설정
        timeLabel = new JLabel("소요 시간: 0.00초", SwingConstants.CENTER); // JLabel 객체를 생성하고 초기 텍스트를 "소요 시간: 0.00초"로 설정
        accuracyLabel = new JLabel("정확도: 0.00%", SwingConstants.CENTER); //JLabel 객체를 생성하고 초기 텍스트를 "정확도: 0.00%"로 설정
        infoPanel.add(timeLabel);
        infoPanel.add(accuracyLabel);
        // infoPanel 에 생성한 JLabel 객체인 timeLabel 과 accuracyLabel 을 추가
        add(infoPanel, BorderLayout.SOUTH); // infoPanel 을 프레임의 남쪽 (BorderLayout.SOUTH)에 추가

        startButton = new JButton("시작"); //JButton 객체를 생성하고 초기 텍스트를 "시작"으로 설정
        startButton.addActionListener(new ActionListener() { //startButton 에 ActionListener 를 추가, 버튼이 클릭될 때 이벤트를 처리하는 코드
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startButton.getText().equals("시작")) {
                    startGame();
                } else {
                    checkResult();   // 현재 버튼의 텍스트가 "시작"인지 확인 후 시작이면 startGame() 메소드를 불러와서 게임시작
                }                    // 아니면 현재 입력된 텍스트의 결과를 확인하는 메서드 checkResult() 를 호출
            }
        });
        add(startButton, BorderLayout.EAST);  //startButton 을 프레임의 동쪽 (BorderLayout.EAST) 에 추가
    }

    private void startGame() {
        sentenceIndex = getRandomSentenceIndex(); //메서드를 호출하여 문장 배열에서 랜덤한 문장을 가져옴
        promptLabel.setText(sentences[sentenceIndex]); //promptLabel 의 텍스트를 랜덤하게 선택된 문장으로 설정 후 게임이 시작되면 사용자가 입력해야 할 문장이 프레임 상단의 라벨에 표시
        textPane.setEnabled(true); //사용자가 텍스트를 입력할 수 있도록 설정
        textPane.setText(""); //textPane 의 텍스트를 빈 문자열로 설정, 기존의 텍스트를 삭제
        textPane.requestFocus(); //textPane 에 포커스를 요청하여 키보드 입력을 받을 수 있게 함
        startTime = System.currentTimeMillis(); //현재 시간을 가져와 startTime 변수에 저장
        startButton.setText("제출"); //startButton 의 텍스트를 "시작" 에서 "제출"로 변경
    }

    private void checkResult() {                 // checkResult 메서드는 사용자가 문장을 입력하고 "제출" 버튼을 클릭했을 때 호출됨
        long endTime = System.currentTimeMillis();  //현재 시간을 측정하여 endTime 에 저장
        double diffTime = (endTime - startTime) / 1000.0; // endTime 에 startTime 을 빼서 소요시간을 계산한 뒤 diffTime 에 저장
        totalElapsedTime += diffTime; // diffTime 을 누적하여 totalElapsedTime 에 저장

        String input = textPane.getText().trim(); // 사용자가 입력한 텍스트를 textPane 에서 가져와 공백을 제거하고 input 변수에 저장
        double accuracy = calculateAccuracy(sentences[sentenceIndex], input); //calculateAccuracy 메서드를 호출하여 사용자가 입력한 텍스트의 정확도를 계산
        totalAccuracy += accuracy; //accuracy 를 누적합하여 totalAccuracy 에 저장
        sentencesCount++; // 입력한 문장 개수를 증가시켜 게임을 진행

        timeLabel.setText("소요 시간: " + String.format("%.2f", diffTime) + "초"); // timeLabel 의 텍스트를 업데이트하여 사용자가 문장을 입력하는 데 걸린 시간을 표시
        accuracyLabel.setText("정확도: " + String.format("%.2f", accuracy) + "%"); // accuracyLabel 의 텍스트를 업데이트하여 사용자가 입력한 문장의 정확도를 표시

        sentenceUsed[sentenceIndex] = true; // 중복된 문장이 나오지 않도록 현재 문장을 이미 사용한 것으로 표시
        textPane.setEnabled(false); // 결과를 확인하는 동안 사용자가 텍스트를 입력하지 못하도록 textPane 을 비활성화

        if (sentencesCount < sentences.length) { // 현재 입력한 문장의 개수가 전체 문장의 개수보다 작은지 확인
            startButton.setText("다음"); //적다면 startButton 텍스트를 "다음" 으로 변경
            startGame(); // startGame 메서드를 호출하여 다음 문장을 시작
        } else {
            showFinalResult(); // showFinalResult 메서드를 호출하여 게임의 최종 결과를 표시
        }
    }

    private void showFinalResult() {
        double averageTime = totalElapsedTime / sentences.length; // 전체 소요 시간 (totalElapsedTime)을 문장의 개수 (sentences.length)로 나누어 평균 소요 시간을 계산
        double averageAccuracy = totalAccuracy / sentences.length; // 전체 정확도 (totalAccuracy)를 문장의 개수로 나누어 평균 정확도를 계산

        JOptionPane.showMessageDialog(this, // JOptionPane.showMessageDialog 메서드를 사용하여 결과를 팝업 메시지로 표시
                "게임 결과:\n전체 소요 시간: " + String.format("%.2f", totalElapsedTime) + "초\n"
                        + "평균 소요 시간: " + String.format("%.2f", averageTime) + "초\n"
                        + "전체 정확도: " + String.format("%.2f", averageAccuracy) + "%",
                "게임 결과", JOptionPane.INFORMATION_MESSAGE);

        startButton.setText("시작"); // startButton 의 텍스트를 "시작"으로 변경하여 게임을 다시 시작할 수 있도록 변경
        promptLabel.setText("게임을 시작하려면 '시작' 버튼을 클릭하세요.");
        totalElapsedTime = 0;
        totalAccuracy = 0;
        sentencesCount = 0;
        sentenceUsed = new boolean[sentences.length];

        // 게임 진행 중 설정 된 값들을 초기화하여 게임을 다시 시작 할 수 있도록 함
    }

    private int getRandomSentenceIndex() {   // getRandomSentenceIndex 메서드는 문장 배열에서 사용되지 않은 문장을 랜덤하게 선택하는 메서드
        int index;
        do {
            index = random.nextInt(sentences.length);
        } while (sentenceUsed[index]);
        return index;
    }
    /*
    1. 랜덤하게 인덱스를 선택
    2. 선택된 인덱스가 이미 사용된 인덱스인지 확인
    3. 만약 이미 사용된 인덱스라면, 다시 랜덤하게 인덱스를 선택
    4. 사용되지 않은 인덱스를 찾을 때까지 반복
    5. 사용되지 않은 인덱스를 반환
     */

    private double calculateAccuracy(String sentence, String input) {
        // calculateAccuracy 메서드는 사용자가 입력한 문자열(input)과 주어진 문장(sentence)을 비교하여 정확도를 계산하는 역할
        int correctChar = 0;
        int maxLength = Math.max(sentence.length(), input.length());

        for (int i = 0; i < maxLength; i++) {
            if (i < sentence.length() && i < input.length() && sentence.charAt(i) == input.charAt(i)) {
                correctChar++;
            }
        }
        double rate = ((double) correctChar / maxLength) * 100;
        return rate;
    }
    /*
    1. 정확히 일치하는 문자 수를 확인(correctChar).
    2. 비교 기준으로 두 문자열 중 더 긴 길이 (maxLength)를 사용
    3. 각 문자를 비교하고 일치하는 경우 correctChar 를 증가 시킴
    4. 정확도를 계산하여 백분율로 변환
    5. 계산된 정확도를 반환
     */

    private void highlightText() {
        // highlightText 메서드는 사용자가 입력한 텍스트를 실시간으로 하이라이트하여, 맞은 부분은 검은색으로, 틀린 부분은 빨간색으로 표시
        String input = textPane.getText(); // 사용자가 현재 textPane 에 입력한 텍스트를 가져옴
        String sentence = sentences[sentenceIndex]; // 현재 사용자가 입력해야 하는 문장을 가져옴
        StyledDocument doc = textPane.getStyledDocument(); //글자 색상 변경을 위한 스타일 속성을 정의하기 위한 객체를 생성
        SimpleAttributeSet correctAttr = new SimpleAttributeSet(); // 올바른 문자를 위한 스타일 속성을 정의하기 위한 객체를 생성
        SimpleAttributeSet incorrectAttr = new SimpleAttributeSet(); // 틀린 문자를 위한 스타일 속성을 정의하기 위한 객체를 생성

        StyleConstants.setForeground(correctAttr, Color.BLACK); // 올바른 문자는 검정색으로 표시
        StyleConstants.setForeground(incorrectAttr, Color.RED); // 틀린 문자는 빨간색으로 표시

        doc.setCharacterAttributes(0, input.length(), correctAttr, true);
        // 입력된 전체 텍스트에 대해 기본적으로 올바른 문자 스타일(검은색)을 적용

        for (int i = 0; i < input.length(); i++) {  // 입력된 각 문자에 대해 반복
            if (i >= sentence.length() || input.charAt(i) != sentence.charAt(i)) {  //  잘못된 문자인 경우를 체크
                doc.setCharacterAttributes(i, 1, incorrectAttr, false);
            }
        }
    }

    public static void main(String[] args) {   // 메인 함수
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String[] sentences = {   // 타자 연습 게임에서 사용될 문장들을 배열로 정의
                        "안녕하세요", "이 게임은 타자 연습 게임입니다.", "나는 누구 입니다", "안녕히 가세요", "다음에 또 와요"
                };
                new TypingGameGUI(sentences).setVisible(true);
            }
        });
    }
}


/*
Java Swing Component Ref : https://blog.naver.com/battledocho/220006946387
Font Ref : https://m.blog.naver.com/10hsb04/221607286384
SetLayout Ref : https://yoo11052.tistory.com/45
GridLayout Ref : https://eating-coding.tistory.com/3
addActionListener Ref : https://blog.naver.com/reeeh/220436764029
currentTimeMillis Ref : https://blog.naver.com/geeyoming/220442373612
trim() 앞 뒤 공백제거 메소드 Ref :  https://chanheumkoon.tistory.com/entry/JAVA-%EB%AC%B8%EC%9E%90%EC%97%B4-%EC%95%9E%EB%92%A4-%EA%B3%B5%EB%B0%B1%EC%A0%9C%EA%B1%B0-%EB%A9%94%EC%84%9C%EB%93%9C-trim
JOptionPane Ref : https://shin-01.tistory.com/34
*/
