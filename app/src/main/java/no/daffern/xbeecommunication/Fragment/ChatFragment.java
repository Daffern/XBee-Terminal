package no.daffern.xbeecommunication.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import no.daffern.xbeecommunication.Adapter.ChatAdapter;
import no.daffern.xbeecommunication.DataTypes;
import no.daffern.xbeecommunication.Listener.XBeeFrameListener;
import no.daffern.xbeecommunication.Model.ChatMessage;
import no.daffern.xbeecommunication.Model.Node;
import no.daffern.xbeecommunication.R;
import no.daffern.xbeecommunication.XBee.Frames.XBeeReceiveFrame;
import no.daffern.xbeecommunication.XBee.Frames.XBeeStatusFrame;
import no.daffern.xbeecommunication.XBee.Frames.XBeeTransmitFrame;
import no.daffern.xbeecommunication.XBeeService;

/**
 * Created by Daffern on 27.05.2016.
 */
public class ChatFragment extends Fragment {

    EditText writeText;

    ListView chatView;
    ChatAdapter chatAdapter;

    TextView nodeText;

    LinkedHashMap<Integer, ArrayList<ChatMessage>> messageMap;

    ArrayList<ChatMessage> unAcknowledgedFrames;

    Node currentNode;
    XBeeService xBeeService;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        initializeInterface();

    }

    public ChatFragment() {
        super();
        xBeeService = XBeeService.getInstance();
        messageMap = new LinkedHashMap<>();
        messageMap.put(xBeeService.broadcastNode.getKey(), new ArrayList<ChatMessage>());

        unAcknowledgedFrames = new ArrayList<>();

        xBeeService.addXBeeFrameListener(new XBeeFrameListener() {

            @Override
            public void onChatMessage(XBeeReceiveFrame frame) {
                int key = Node.getKey(frame.getAddress64());

                ArrayList<ChatMessage> messages;
                if (frame.isBroadcast()) {
                    messages = messageMap.get(xBeeService.broadcastNode.getKey());

                } else {
                    messages = messageMap.get(key);
                }

                if (messages == null) {
                    messages = new ArrayList<>();
                    messageMap.put(key, messages);
                }
                ChatMessage chatMessage = new ChatMessage(true, new String(frame.getRfData()), "", frame.getFrameId(), System.currentTimeMillis());

                messages.add(chatMessage);

                updateUI();
            }

            @Override
            public void onTransmitStatus(XBeeStatusFrame frame) {

                int frameId = frame.getFrameId();

                for (int i = unAcknowledgedFrames.size() - 1; i >= 0 ; i--) {

                    ChatMessage chatMessage = unAcknowledgedFrames.get(i);

                    if (chatMessage.frameId == frameId) {

                        if (frame.getDeliveryStatus() == XBeeStatusFrame.SUCCESS) {
                            chatMessage.status = "Sent ";
                            chatMessage.time = System.currentTimeMillis();
                        } else {
                            chatMessage.status = "Failed with code: " + frame.getDeliveryStatus();
                        }
                        updateUI();
                        unAcknowledgedFrames.remove(i);
                        break;
                    }
                }

            }

        });
    }

    public void setCurrentNode(Node node) {
        this.currentNode = node;
        if (chatAdapter != null) {
            chatAdapter.setMessages(messageMap.get(currentNode.getKey()));
        }
    }
    public void updateUI(){
        if (getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (chatAdapter != null) {
                    setCurrentNode(currentNode);
                    chatAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    public void sendMessage(String message) {
        byte[] bytes = message.getBytes();

        XBeeTransmitFrame transmitFrame = new XBeeTransmitFrame(DataTypes.APP_CHAT_MESSAGE);
        transmitFrame.setRfData(bytes);
        transmitFrame.setAddress64(currentNode.address64);
        transmitFrame.setDataType(DataTypes.APP_CHAT_MESSAGE);

        //check if send was successful
        if (xBeeService.sendFrame(transmitFrame)) {


            ChatMessage chatMessage = new ChatMessage(false, message, "Sending...", transmitFrame.getFrameId(), 0);
            unAcknowledgedFrames.add(chatMessage);

            ArrayList<ChatMessage> messages = messageMap.get(currentNode.getKey());
            if (messages == null) {
                messages = new ArrayList<>();
                messageMap.put(currentNode.getKey(), messages);
            }

            messages.add(chatMessage);
            updateUI();
        }

        writeText.getText().clear();

    }

    private void initializeInterface() {
        writeText = (EditText) getView().findViewById(R.id.editText);
        writeText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        InputFilter inputFilter = new InputFilter.LengthFilter(XBeeTransmitFrame.MAX_RF_DATA-1);
        writeText.setFilters(new InputFilter[]{inputFilter});
        writeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    String message = writeText.getText().toString();
                    if (message.length() > 0) {
                        sendMessage(message);
                        return true;
                    }
                }
                return false;
            }
        });

        nodeText = (TextView) getView().findViewById(R.id.nodeText);
        nodeText.setText("Chatting with: " + currentNode.getNodeIdentifier());


        chatView = (ListView) getView().findViewById(R.id.chatList);
        chatView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        chatView.setStackFromBottom(true);

        chatAdapter = new ChatAdapter(getActivity(), R.id.chatList);
        chatView.setAdapter(chatAdapter);

        //update list interface for current node
        setCurrentNode(currentNode);
        chatAdapter.notifyDataSetChanged();


    }


}
