package org.jhm69.battle_of_quiz.adapters;import android.app.ProgressDialog;import android.content.Context;import android.content.Intent;import android.util.Log;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.view.animation.Animation;import android.view.animation.AnimationUtils;import android.widget.ImageView;import android.widget.TextView;import androidx.annotation.NonNull;import androidx.recyclerview.widget.RecyclerView;import com.afollestad.materialdialogs.MaterialDialog;import com.bumptech.glide.Glide;import com.bumptech.glide.request.RequestOptions;import com.github.marlonlom.utilities.timeago.TimeAgo;import com.google.firebase.auth.FirebaseAuth;import com.google.firebase.auth.FirebaseUser;import com.google.firebase.firestore.FirebaseFirestore;import org.jhm69.battle_of_quiz.R;import org.jhm69.battle_of_quiz.models.Comment;import org.jhm69.battle_of_quiz.ui.activities.friends.FriendProfile;import org.jhm69.battle_of_quiz.utils.MathView;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.Objects;import de.hdodenhof.circleimageview.CircleImageView;import es.dmoral.toasty.Toasty;public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {    private final List<Comment> commentList;    private final Context context;    private final boolean isOwner;    int lastPosition = -1;    private FirebaseFirestore mFirestore;    private FirebaseUser mCurrentUser;    public CommentsAdapter(List<Comment> commentList, Context context, boolean owner) {        this.commentList = commentList;        this.context = context;        this.isOwner = owner;    }    @NonNull    @Override    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {        mFirestore = FirebaseFirestore.getInstance();        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);        return new ViewHolder(view);    }    @Override    public long getItemId(int position) {        return position;    }    @Override    public int getItemViewType(int position) {        return position;    }    @Override    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {        Animation animation = AnimationUtils.loadAnimation(context,                (position > lastPosition) ? R.anim.up_from_bottom                        : R.anim.down_from_top);        holder.itemView.startAnimation(animation);        lastPosition = position;        if (isOwner) {            enableDeletion(holder);        } else {            if (commentList.get(position).getId().equals(mCurrentUser.getUid())) {                enableDeletion(holder);            }        }        holder.username.setText(commentList.get(position).getUsername());        holder.username.setOnClickListener(v -> context.startActivity(new Intent(context, FriendProfile.class).putExtra("f_id", commentList.get(holder.getAdapterPosition()).getId())));        holder.image.setOnClickListener(v -> context.startActivity(new Intent(context, FriendProfile.class).putExtra("f_id", commentList.get(holder.getAdapterPosition()).getId())));        Glide.with(context)                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo))                .load(commentList.get(position).getImage())                .into(holder.image);        holder.comment.setDisplayText(commentList.get(position).getComment());        String timeAgo = TimeAgo.using(Long.parseLong(commentList.get(position).getTimestamp()));        holder.timestamp.setText(timeAgo);        try {            mFirestore.collection("Users")                    .document(commentList.get(position).getId())                    .get()                    .addOnSuccessListener(documentSnapshot -> {                        try {                            if (!Objects.equals(documentSnapshot.getString("username"), commentList.get(holder.getAdapterPosition()).getUsername()) &&                                    !Objects.equals(documentSnapshot.getString("image"), commentList.get(holder.getAdapterPosition()).getImage())) {                                Map<String, Object> commentMap = new HashMap<>();                                commentMap.put("username", documentSnapshot.getString("username"));                                commentMap.put("image", documentSnapshot.getString("image"));                                mFirestore.collection("Posts")                                        .document(commentList.get(holder.getAdapterPosition()).getPost_id())                                        .collection("Comments")                                        .document(commentList.get(holder.getAdapterPosition()).commentId)                                        .update(commentMap)                                        .addOnSuccessListener(aVoid -> Log.i("comment_update", "success"))                                        .addOnFailureListener(e -> Log.i("comment_update", "failure"));                                holder.username.setText(documentSnapshot.getString("username"));                                Glide.with(context)                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo))                                        .load(documentSnapshot.getString("image"))                                        .into(holder.image);                            } else if (!Objects.equals(documentSnapshot.getString("username"), commentList.get(holder.getAdapterPosition()).getUsername())) {                                Map<String, Object> commentMap = new HashMap<>();                                commentMap.put("username", documentSnapshot.getString("username"));                                mFirestore.collection("Posts")                                        .document(commentList.get(holder.getAdapterPosition()).getPost_id())                                        .collection("Comments")                                        .document(commentList.get(holder.getAdapterPosition()).commentId)                                        .update(commentMap)                                        .addOnSuccessListener(aVoid -> Log.i("comment_update", "success"))                                        .addOnFailureListener(e -> Log.i("comment_update", "failure"));                                holder.username.setText(documentSnapshot.getString("username"));                            } else if (!Objects.equals(documentSnapshot.getString("image"), commentList.get(holder.getAdapterPosition()).getImage())) {                                Map<String, Object> commentMap = new HashMap<>();                                commentMap.put("image", documentSnapshot.getString("image"));                                mFirestore.collection("Posts")                                        .document(commentList.get(holder.getAdapterPosition()).getPost_id())                                        .collection("Comments")                                        .document(commentList.get(holder.getAdapterPosition()).commentId)                                        .update(commentMap)                                        .addOnSuccessListener(aVoid -> Log.i("comment_update", "success"))                                        .addOnFailureListener(e -> Log.i("comment_update", "failure"));                                Glide.with(context)                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo))                                        .load(documentSnapshot.getString("image"))                                        .into(holder.image);                            }                        } catch (Exception e) {                            e.printStackTrace();                        }                    })                    .addOnFailureListener(e -> Log.e("Error", e.getMessage()));        } catch (Exception ex) {            Log.w("error", "fastscrolled", ex);        }    }    private void enableDeletion(final ViewHolder holder) {        holder.delete.setVisibility(View.VISIBLE);        holder.delete.setAlpha(0.0f);        holder.delete.animate()                .alpha(1.0f)                .start();        holder.delete.setOnClickListener(v -> new MaterialDialog.Builder(context)                .title("Delete comment")                .content("Are you sure do you want to delete your comment?")                .positiveText("Yes")                .negativeText("No")                .onPositive((dialog, which) -> {                    dialog.dismiss();                    final ProgressDialog progressDialog = new ProgressDialog(context);                    progressDialog.setMessage("Deleting comment...");                    progressDialog.setIndeterminate(true);                    progressDialog.show();                    mFirestore.collection("Posts")                            .document(commentList.get(holder.getAdapterPosition()).getPost_id())                            .collection("Comments")                            .document(commentList.get(holder.getAdapterPosition()).commentId)                            .delete()                            .addOnSuccessListener(aVoid -> {                                commentList.remove(holder.getAdapterPosition());                                Toasty.success(context, "Comment deleted", Toasty.LENGTH_SHORT, true).show();                                notifyDataSetChanged();                                progressDialog.dismiss();                            })                            .addOnFailureListener(e -> {                                progressDialog.dismiss();                                Toasty.error(context, "Error deleting comment: " + e.getLocalizedMessage(), Toasty.LENGTH_SHORT, true).show();                                Log.w("Error", "delete comment", e);                            });                })                .onNegative((dialog, which) -> dialog.dismiss())                .show());    }    @Override    public int getItemCount() {        return commentList.size();    }    public static class ViewHolder extends RecyclerView.ViewHolder {        private final CircleImageView image;        private final TextView username;        private final TextView timestamp;        private final ImageView delete;        final MathView comment;        public ViewHolder(View itemView) {            super(itemView);            image = itemView.findViewById(R.id.comment_user_image);            username = itemView.findViewById(R.id.comment_username);            comment = itemView.findViewById(R.id.comment_text);            timestamp = itemView.findViewById(R.id.comment_timestamp);            delete = itemView.findViewById(R.id.delete);        }    }}