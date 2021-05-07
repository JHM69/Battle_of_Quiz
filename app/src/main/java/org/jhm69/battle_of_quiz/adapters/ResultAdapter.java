package org.jhm69.battle_of_quiz.adapters;import android.annotation.SuppressLint;import android.app.Activity;import android.app.ActivityOptions;import android.content.Context;import android.content.Intent;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.view.animation.Animation;import android.view.animation.AnimationUtils;import android.widget.ImageView;import android.widget.TextView;import android.widget.Toast;import androidx.annotation.NonNull;import androidx.constraintlayout.widget.ConstraintLayout;import androidx.fragment.app.FragmentActivity;import androidx.lifecycle.LifecycleOwner;import androidx.lifecycle.ViewModelProviders;import androidx.recyclerview.widget.RecyclerView;import com.afollestad.materialdialogs.MaterialDialog;import com.bumptech.glide.Glide;import com.bumptech.glide.request.RequestOptions;import com.github.marlonlom.utilities.timeago.TimeAgo;import com.google.firebase.database.FirebaseDatabase;import com.google.firebase.firestore.FirebaseFirestore;import org.jhm69.battle_of_quiz.R;import org.jhm69.battle_of_quiz.ui.activities.quiz.QuizBattle;import org.jhm69.battle_of_quiz.ui.activities.quiz.Result;import org.jhm69.battle_of_quiz.ui.activities.quiz.ResultActivity;import org.jhm69.battle_of_quiz.viewmodel.BattleViewModel;import org.jhm69.battle_of_quiz.viewmodel.ResultViewModel;import org.jhm69.battle_of_quiz.viewmodel.UserViewModel;import java.util.List;import java.util.Objects;import de.hdodenhof.circleimageview.CircleImageView;import es.dmoral.toasty.Toasty;import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.MyViewHolder> {    public static final int JUST_STARTED = -1;    public static final int OFFLINE_STARTED = -2;    public static final int JUST_IN = -3;    public static final int IN_STARTED = -4;    public static final int COMPLETED = -5;    private final List<Result> results;    private final Context context;    boolean visitor = false;    int lastPosition = -1;    boolean canDelete = false;    public ResultAdapter(List<Result> results, Context context, boolean visitor) {        this.results = results;        this.context = context;        this.visitor = visitor;    }    @NonNull    @Override    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_result, viewGroup, false);        return new MyViewHolder(view);    }    @SuppressLint({"SetTextI18n", "UseCompatLoadingForColorStateLists"})    @Override    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {        Animation animation = AnimationUtils.loadAnimation(context,                (i > lastPosition) ? R.anim.up_from_bottom                        : R.anim.down_from_top);        holder.itemView.startAnimation(animation);        lastPosition = i;        //int lastPosition = -1;        //my - sender        //other - receiver        boolean play = false;        boolean result = false;        boolean offline = false;        Result model = results.get(i);        setUserData(holder.myImg, holder.myName, holder.thisLevel, userId, true);        String timeAgo = TimeAgo.using(Long.parseLong(model.getTimestamp()));        if (model.getAction() == JUST_STARTED) {            result = true;            holder.linearLayout.setBackgroundTintList(context.getResources().getColorStateList(R.color.your_invites));            holder.mySc.setText(String.valueOf(model.getMyScore()));            holder.infoTv.setText("You challenged in " + model.getTopic() + " " + timeAgo);            holder.othSc.setText("-");            setUserData(holder.otherImg, holder.otherName, holder.otherLevel, model.getOtherUid(), false);        } else if (model.getAction() == OFFLINE_STARTED) {            play = true;            offline = true;            holder.linearLayout.setBackgroundTintList(context.getResources().getColorStateList(R.color.pending));            holder.mySc.setText(String.valueOf(model.getMyScore()));            holder.infoTv.setText("You started a match in " + model.getTopic() + " " + timeAgo);            holder.othSc.setText("-");            setUserData(holder.otherImg, holder.otherName, holder.otherLevel, model.getOtherUid(), false);        } else if (model.getAction() == COMPLETED) {            result = true;            holder.linearLayout.setBackgroundTintList(context.getResources().getColorStateList(R.color.completed));            holder.mySc.setText(String.valueOf(model.getMyScore()));            holder.infoTv.setText("Completed a match in " + model.getTopic() + " " + timeAgo);            holder.othSc.setText(String.valueOf(model.getOtherScore()));            setUserData(holder.otherImg, holder.otherName, holder.otherLevel, model.getOtherUid(), false);        } else if (model.getAction() == JUST_IN) {            play = true;            holder.linearLayout.setBackgroundTintList(context.getResources().getColorStateList(R.color.invite));            holder.othSc.setText("?");            holder.infoTv.setText("Invited you in " + model.getTopic() + " " + timeAgo);            holder.mySc.setText("-");            setUserData(holder.otherImg, holder.otherName, holder.otherLevel, model.getOtherUid(), false);        } else if (model.getAction() == IN_STARTED) {            play = true;            offline = true;            holder.linearLayout.setBackgroundTintList(context.getResources().getColorStateList(R.color.my_pending));            holder.othSc.setText("?");            holder.infoTv.setText("Accepted but not Completed " + model.getTopic() + " " + timeAgo);            holder.mySc.setText(String.valueOf(model.getMyScore()));            setUserData(holder.otherImg, holder.otherName, holder.otherLevel, model.getMyUid(), false);        }        holder.itemView.setOnLongClickListener(view -> {            ResultViewModel resultViewModel = ViewModelProviders.of((FragmentActivity) context).get(ResultViewModel.class);            BattleViewModel battleVM = ViewModelProviders.of((FragmentActivity) context).get(BattleViewModel.class);            new MaterialDialog.Builder(context)                    .title("Clear this Result")                    .content("Are you sure do you want to delete this Result?")                    .positiveText("Yes")                    .negativeText("No")                    .onPositive((dialog, which) -> {                                if (System.currentTimeMillis() - Long.parseLong(model.getTimestamp()) >= 86400000) {                                    canDelete = true;                                }                                if (model.getAction() == COMPLETED || model.getAction() == OFFLINE_STARTED || canDelete) {                                    FirebaseDatabase.getInstance().getReference().child("Result").child(userId).child(model.getBattleId()).removeValue().addOnCompleteListener(task -> {                                        resultViewModel.delete(model);                                        battleVM.delete(model.getBattleId());                                        Toasty.success(context, "Deleted", Toast.LENGTH_SHORT).show();                                        notifyDataSetChanged();                                    });                                } else {                                    Toasty.error(context, "You can't delete it right now. Complete the battle first. Or wait at least 24h to get delete access.", Toast.LENGTH_SHORT).show();                                }                            }                    )                    .show();            return false;        });        boolean finalResult = result;        boolean finalOffline = offline;        boolean finalPlay = play;        holder.itemView.setOnClickListener(view -> {            if (finalPlay && finalOffline) {                Intent intent = new Intent(context, QuizBattle.class);                intent.putExtra("ofo", model.getBattleId());                context.startActivity(intent,  ActivityOptions.makeSceneTransitionAnimation((Activity)context).toBundle());            } else if (finalResult) {                Intent intent = new Intent(context, ResultActivity.class);                intent.putExtra("resultId", model.getBattleId());                context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity)context).toBundle());            } else if (finalPlay) {                Intent intent = new Intent(context, QuizBattle.class);                intent.putExtra("battleId", model.getBattleId());                context.startActivity(intent,  ActivityOptions.makeSceneTransitionAnimation((Activity)context).toBundle());            }        });    }    @Override    public int getItemCount() {        return results.size();    }    private void setUserData(ImageView proPic, TextView name, TextView level, String uid, boolean me) {        try {            if (me) {                UserViewModel userViewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);                userViewModel.user.observe((LifecycleOwner) context, me1 -> {                    name.setText(me1.getUsername());                    level.setText(String.valueOf(getLevelNum((int) me1.getScore())));                    Glide.with(context)                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))                            .load(me1.getImage())                            .into(proPic);                });            } else {                FirebaseFirestore.getInstance().collection("Users")                        .document(uid)                        .get()                        .addOnSuccessListener(documentSnapshot -> {                            try {                                level.setText(String.valueOf(getLevelNum(Objects.requireNonNull(documentSnapshot.getLong("score")).intValue())));                                name.setText(documentSnapshot.getString("username"));                            } catch (Exception ignored) {                            }                            Glide.with(context)                                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo_icon))                                    .load(documentSnapshot.getString("image"))                                    .into(proPic);                        });            }        } catch (NullPointerException ignored) {        }    }    public int getLevelNum(int score) {        final int level;        if (score <= 500) {            level = 1;        } else if (score <= 1000) {            level = 2;        } else if (score <= 1500) {            level = 3;        } else if (score <= 2000) {            level = 4;        } else if (score <= 2500) {            level = 5;        } else if (score <= 3500) {            level = 6;        } else if (score <= 5000) {            level = 7;        } else {            level = -1;        }        return level;    }    public static class MyViewHolder extends RecyclerView.ViewHolder {        public final View mView;        public final CircleImageView myImg;        public final CircleImageView otherImg;        public final TextView myName;        public final TextView otherName;        public final TextView infoTv;        public final TextView mySc;        public final TextView othSc;        public final TextView thisLevel;        public final TextView otherLevel;        final ConstraintLayout linearLayout;        public MyViewHolder(View itemView) {            super(itemView);            mView = itemView;            myImg = mView.findViewById(R.id.thisUserImage);            otherImg = mView.findViewById(R.id.otherUserImage);            thisLevel = mView.findViewById(R.id.ThislevelTv);            otherLevel = mView.findViewById(R.id.otherlevelTv);            myName = mView.findViewById(R.id.thisUserName);            otherName = mView.findViewById(R.id.otherUserName);            infoTv = mView.findViewById(R.id.ivz);            mySc = mView.findViewById(R.id.myScore);            othSc = mView.findViewById(R.id.otherScore);            linearLayout = mView.findViewById(R.id.linearLayout4);        }    }    /*     *  -1 -> JUST_STARTED -> Result     *  -2 -> OFFLINE_STARTED -> Play     *  -3 -> JUST_IN -> Play     *  -4 -> IN_STARTED -> Play     *  -5 -5 COMPLETED -> Result     * */}