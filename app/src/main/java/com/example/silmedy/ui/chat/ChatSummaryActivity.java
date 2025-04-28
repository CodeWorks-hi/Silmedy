public class ChatSummaryActivity extends AppCompatActivity {

    private TextView textViewSummary;
    private ChatHistoryManager chatHistoryManager;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_summary);

        // 관리자 인증
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null || !user.getEmail().equals("admin@yourdomain.com")) {
            finish(); // 관리자가 아니면 바로 종료
            return;
        }

        textViewSummary = findViewById(R.id.textViewSummary);
        chatHistoryManager = new ChatHistoryManager();
        sessionId = getIntent().getStringExtra("SESSION_ID");

        if (sessionId != null) {
            chatHistoryManager.loadChatHistory(sessionId, summary -> {
                textViewSummary.setText(summary);
            });
        }
    }
}
