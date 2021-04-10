package org.jhm69.battle_of_quiz.ui.activities.post;import android.annotation.SuppressLint;import android.content.Context;import android.content.Intent;import android.os.Bundle;import android.util.AndroidRuntimeException;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.view.Window;import android.view.WindowManager;import android.widget.ImageView;import android.widget.ProgressBar;import android.widget.TextView;import androidx.annotation.NonNull;import androidx.appcompat.app.AppCompatActivity;import androidx.appcompat.widget.Toolbar;import androidx.core.content.ContextCompat;import androidx.recyclerview.widget.LinearLayoutManager;import androidx.recyclerview.widget.RecyclerView;import com.bumptech.glide.Glide;import com.bumptech.glide.request.RequestOptions;import com.google.firebase.firestore.DocumentChange;import com.google.firebase.firestore.FirebaseFirestore;import org.jhm69.battle_of_quiz.R;import org.jhm69.battle_of_quiz.models.Player;import org.jhm69.battle_of_quiz.ui.activities.friends.FriendProfile;import java.util.ArrayList;import java.util.List;import es.dmoral.toasty.Toasty;import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;public class WhoLikedActivity extends AppCompatActivity {    @SuppressLint("CheckResult")    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_who_liked);        Toolbar toolbar = findViewById(R.id.toolbar3);        Window window = this.getWindow();        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusBar));        ProgressBar loading = findViewById(R.id.progressBar4);        RecyclerView recyclerView = findViewById(R.id.liked_list);        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));        recyclerView.setLayoutManager(layoutManager);        recyclerView.setHasFixedSize(true);        String postId = getIntent().getStringExtra("postId");        String type = getIntent().getStringExtra("type");        List<Player> playerList = new ArrayList<>();        if (type.equals("Liked_Users")) {            toolbar.setTitle("Quizzers who Liked this post");        } else {            toolbar.setTitle("Quizzers who Saved this post");        }        try {            FirebaseFirestore.getInstance().collection("Posts")                    .document(postId)                    .collection(type)                    .get()                    .addOnSuccessListener(queryDocumentSnapshots -> {                        for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {                            FirebaseFirestore.getInstance().collection("Users")                                    .document(doc.getDocument().getId())                                    .get()                                    .addOnSuccessListener(documentSnapshot -> {                                        try {                                            Player player = new Player(doc.getDocument().getId(), documentSnapshot.getString("name"), documentSnapshot.getString("image"), documentSnapshot.getLong("score"));                                            playerList.add(player);                                            UserAdapterw jhu = new UserAdapterw(playerList, getApplicationContext());                                            recyclerView.setAdapter(jhu);                                            jhu.notifyDataSetChanged();                                            loading.setVisibility(View.GONE);                                        } catch (NullPointerException ignored) {                                        }                                    });                        }                    });        } catch (NullPointerException ignored) {            Toasty.error(getApplicationContext(), "Failed to load", Toasty.LENGTH_LONG, false);        }    }}class UserAdapterw extends RecyclerView.Adapter<UserAdapterw.MyViewHolder> {    private final List<Player> playerList;    private final Context context;    private String check, otherUid;    public UserAdapterw(List<Player> playerList, Context context) {        this.playerList = playerList;        this.context = context;    }    @NonNull    @Override    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user_liked, viewGroup, false);        return new MyViewHolder(view);    }    @SuppressLint("SetTextI18n")    @Override    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {        try {            final Player player = playerList.get(i);            if (!player.getId().equals(userId)) {                viewHolder.nameTV.setText(player.getName());            } else {                viewHolder.nameTV.setText("You");            }            viewHolder.levelTv.setText(String.valueOf(getLevelNum((int) player.getScore())));            viewHolder.itemView.setOnClickListener(view -> {                if (!player.getId().equals(userId)) {                    context.startActivity(new Intent(context, FriendProfile.class).putExtra("f_id", player.getId()).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));                }            });            Glide.with(context)                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))                    .load(player.getImage())                    .into(viewHolder.image);        } catch (AndroidRuntimeException ignored) {        }    }    @Override    public int getItemCount() {        return playerList.size();    }    public Integer getLevelNum(int score) {        final int level;        if (score <= 500) {            level = 1;        } else if (score <= 1000) {            level = 2;        } else if (score <= 1500) {            level = 3;        } else if (score <= 2000) {            level = 4;        } else if (score <= 2500) {            level = 5;        } else if (score <= 3500) {            level = 6;        } else if (score <= 5000) {            level = 7;        } else {            level = -1;        }        return level;    }    public static class MyViewHolder extends RecyclerView.ViewHolder {        final TextView nameTV;        final ImageView image;        final TextView levelTv;        public MyViewHolder(View itemView) {            super(itemView);            nameTV = itemView.findViewById(R.id.name);            image = itemView.findViewById(R.id.image);            levelTv = itemView.findViewById(R.id.levelTv);        }    }}