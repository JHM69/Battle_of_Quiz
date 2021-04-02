package org.jhm69.battle_of_quiz.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.marcoscg.dialogsheet.DialogSheet;

import org.jhm69.battle_of_quiz.R;
import org.jhm69.battle_of_quiz.adapters.ResultAdapter;
import org.jhm69.battle_of_quiz.models.Friends;
import org.jhm69.battle_of_quiz.ui.activities.quiz.PlayQuiz;
import org.jhm69.battle_of_quiz.ui.activities.quiz.SelectTopic;
import org.jhm69.battle_of_quiz.viewmodel.ResultViewModel;
import org.jhm69.battle_of_quiz.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import es.dmoral.toasty.Toasty;

import static org.jhm69.battle_of_quiz.ui.activities.MainActivity.userId;

/**
 * Created by jhm69
 */

public class Quiz extends Fragment {
    ResultViewModel mViewModel;
    UserViewModel userViewModel;
    ExtendedFloatingActionButton button;
    ImageView pro_pic, cover;
    TextView nameTV, scoreTV, levelTV, coverTxt;
    String string;
    /*  void setUpChartData(PieChart pieChart, float win, float lose, float draw) {
            if (win == 0 && lose == 0 && draw == 0) {
                pieChart.setVisibility(View.INVISIBLE);
            } else {
                pieChart.setVisibility(View.VISIBLE);
                Description description = new Description();
                description.setText("");
                pieChart.setDescription(description);
                Map<String, Float> scoreData = new HashMap<>();
                scoreData.put("Win", win);
                scoreData.put("Draw:", draw);
                scoreData.put("Lose:", lose);
                ArrayList<PieEntry> entries = new ArrayList<>();
                if (win != 0) {
                    entries.add(new PieEntry(win, "Win"));
                }
                if (draw != 0) {
                    entries.add(new PieEntry(draw, "Draw"));
                }
                if (lose != 0) {
                    entries.add(new PieEntry(lose, "Lose"));
                }
                PieDataSet pieDataSet = new PieDataSet(entries, " | Won:" + (win == 0 ? "0" : (int) win) + " | Drawn:" + (draw == 0 ? "0" : (int) draw) + " | Loosed:" + (lose == 0 ? "0" : (int) lose) + " | Total Played:" + ((win + draw + lose) == 0 ? "0" : (int) (win + draw + lose)));
                pieDataSet.setColors(Color.parseColor("#00B311"), Color.parseColor("#2196f3"), Color.parseColor("#D32F2F"));
                PieData pieData = new PieData(pieDataSet);
                pieChart.setData(pieData);
                pieData.setValueTextColor(Color.parseColor("#ffffff"));
                pieData.setValueTextSize(8);
                pieChart.setUsePercentValues(true);
                pieChart.animateXY(1500, 1500);
                pieChart.invalidate();
            }
        }
    */
    String tag = "All";
    private RecyclerView mRecyclerView;
    private ResultAdapter resultAdapter;
    private List<Friends> usersList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @SuppressLint({"CheckResult", "UseCompatLoadingForDrawables"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()))
                .get(ResultViewModel.class);
        userViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()))
                .get(UserViewModel.class);
        pro_pic = view.findViewById(R.id.profile_picture);
        nameTV = view.findViewById(R.id.name);
        cover = view.findViewById(R.id.imageView8);
        coverTxt = view.findViewById(R.id.textView15);

        scoreTV = view.findViewById(R.id.score);
        levelTV = view.findViewById(R.id.level);
        button = view.findViewById(R.id.soihefawiw);
        mRecyclerView = view.findViewById(R.id.res);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ChipGroup chipGroup = view.findViewById(R.id.filter_chip_SS_group);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            try {
                Chip c = view.findViewById(checkedId);
                tag = c.getText().toString();
                setupAdapter(tag);
            } catch (NullPointerException j) {

            }
        });

        AdView adView = new AdView(getContext());

        adView.setAdSize(AdSize.BANNER);

        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        MobileAds.initialize(getContext(), initializationStatus -> {
        });

        adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        userViewModel.user.observe(getActivity(), users -> {
            RandomiseListAsyncTask mAsyncTask = new RandomiseListAsyncTask(users.getType());
            Log.d("UsersList:", String.valueOf(users.getType()));
            try {
                usersList = mAsyncTask.execute().get();
            } catch (ExecutionException | InterruptedException e) {
                Log.d("UsersList:", String.valueOf(usersList.size()));
                e.printStackTrace();
            }

            button.setOnClickListener(view1 ->
                    new DialogSheet(Objects.requireNonNull(getActivity()))
                            .setTitle("Chose the opponent")
                            .setMessage("You can play with random players or you can select on your own.")
                            .setRoundedCorners(true)
                            .setColoredNavigationBar(true)
                            .setCancelable(true)
                            .setPositiveButton("Random Player", v -> {

                                if (usersList.size() > 0) {
                                    try {
                                        Friends friends = usersList.get(0);
                                        Collections.shuffle(usersList);
                                        Toasty.info(getActivity(), "You are playing with " + friends.getName() + ", from " + friends.getDept() + ", " + friends.getInstitute() + ". Level: " + getLevelNum(friends.getScore()), Toasty.LENGTH_LONG).show();
                                        Intent goBattle = new Intent(getActivity(), SelectTopic.class);
                                        goBattle.putExtra("otherUid", friends.userId);
                                        goBattle.putExtra("type", friends.getType());
                                        startActivity(goBattle);
                                    } catch (NullPointerException j) {
                                        Toasty.error(Objects.requireNonNull(getContext()), "Error getting random player, Try other option.", Toasty.LENGTH_SHORT);
                                    }
                                } else {
                                    Toasty.error(getActivity(), "Error getting random player, Try custom selection", Toasty.LENGTH_SHORT);
                                    loadFragment(new PlayQuiz());
                                }
                            })
                            .setNegativeButton("Custom Selection", v -> {
                                loadFragment(new PlayQuiz());
                            })
                            .show());

            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
            mRecyclerView.setHasFixedSize(true);
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(true);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
            mViewModel.results.observe(Objects.requireNonNull(getActivity()), result -> {
                resultAdapter = new ResultAdapter(result, getActivity(), false);
                mRecyclerView.setAdapter(resultAdapter);
                resultAdapter.notifyDataSetChanged();
            });
            loadProfileData();
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setupAdapter(String tag) {

        switch (tag) {
            case "Invites":
                mViewModel.getInvites().observe(Objects.requireNonNull(getActivity()), result -> {
                    if (result.size() < 1) {
                        string = "No Invites found";
                        // Toasty.info(getContext(), "No Invites found.", Toast.LENGTH_LONG).show();
                    }
                    resultAdapter = new ResultAdapter(result, getActivity(), false);
                    mRecyclerView.setAdapter(resultAdapter);
                    resultAdapter.notifyDataSetChanged();
                });
                break;
            case "Completed":
                mViewModel.getCompleted().observe(Objects.requireNonNull(getActivity()), result -> {
                    if (result.size() < 1) {
                        string = "No battle Completed yet";
                    }
                    resultAdapter = new ResultAdapter(result, getActivity(), false);
                    mRecyclerView.setAdapter(resultAdapter);
                    resultAdapter.notifyDataSetChanged();
                });
                break;
            case "Pending":
                mViewModel.getPending().observe(Objects.requireNonNull(getActivity()), result -> {
                    if (result.size() < 1) {
                        string = "No Pending found";
                        // Toasty.info(getContext(), "", Toast.LENGTH_LONG).show();
                    }
                    resultAdapter = new ResultAdapter(result, getActivity(), false);
                    mRecyclerView.setAdapter(resultAdapter);
                    resultAdapter.notifyDataSetChanged();
                });
                break;
            case "Your Invites":
                mViewModel.getMyCompleted().observe(Objects.requireNonNull(getActivity()), result -> {
                    if (result.size() < 1) {
                        string = "Empty Your Invites. Invite to others to Play battle Quiz";
                        //Toasty.info(getContext(), "", Toast.LENGTH_LONG).show();
                    }
                    resultAdapter = new ResultAdapter(result, getActivity(), false);
                    mRecyclerView.setAdapter(resultAdapter);
                    resultAdapter.notifyDataSetChanged();
                });
                break;
            case "My Pending":
                mViewModel.getMyPending().observe(Objects.requireNonNull(getActivity()), result -> {
                    if (result.size() < 1) {
                        string = "No my pending found";
                        //Toasty.info(getContext(), "", Toast.LENGTH_LONG).show();
                    }
                    resultAdapter = new ResultAdapter(result, getActivity(), false);
                    mRecyclerView.setAdapter(resultAdapter);
                    resultAdapter.notifyDataSetChanged();
                });
                break;
            case "All":
                mViewModel.results.observe(Objects.requireNonNull(getActivity()), result -> {
                    if (result.size() < 1) {
                        string = getContext().getString(R.string.let_s_challenge_others_in_battle_of_quiz);
                        //Toasty.info(getContext(), "", Toast.LENGTH_LONG).show();
                    }
                    resultAdapter = new ResultAdapter(result, getActivity(), false);
                    mRecyclerView.setAdapter(resultAdapter);
                    resultAdapter.notifyDataSetChanged();
                });
                break;

        }
        if (resultAdapter.getItemCount() == 0) {
            cover.setImageDrawable(getResources().getDrawable(R.drawable.cover));
            coverTxt.setText(string);
        } else {
            coverTxt.setText("");
            coverTxt.setVisibility(View.GONE);
            cover.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadProfileData() {
        try {
            userViewModel.user.observe(Objects.requireNonNull(getActivity()), user -> {
                try {
                    String name = user.getName();
                    String image = user.getImage();
                    int score = (int) user.getScore();

                    //  setUpChartData(pieChart, win, lose, draw);
                    nameTV.setText(name);
                    Glide.with(Objects.requireNonNull(getActivity()))
                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.ic_logo))
                            .load(image)
                            .into(pro_pic);
                    scoreTV.setText("Score: " + score);
                    setLevelByScore(levelTV, score);
                } catch (Exception ignored) {

                }

            });

        } catch (NullPointerException h) {

        }
    }

    private void loadFragment(Fragment fragment) {
        Objects.requireNonNull(getFragmentManager())
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }


    @SuppressLint("SetTextI18n")
    private void setLevelByScore(TextView levelTV, int score) {
        if (score <= 500) {
            levelTV.setText("Level: 1");
        } else if (score <= 1000) {
            levelTV.setText("Level: 2");
        } else if (score <= 1500) {
            levelTV.setText("Level: 3");
        } else if (score <= 2000) {
            levelTV.setText("Level: 4");
        } else if (score <= 2500) {
            levelTV.setText("Level: 5");
        } else if (score <= 3500) {
            levelTV.setText("Level: 6");
        } else if (score <= 5000) {
            levelTV.setText("Level: 7");
        } else {
            levelTV.setText("Level: unknown");
        }
    }

    public Integer getLevelNum(int score) {
        final int level;
        if (score <= 500) {
            level = 1;
        } else if (score <= 1000) {
            level = 2;
        } else if (score <= 1500) {
            level = 3;
        } else if (score <= 2000) {
            level = 4;
        } else if (score <= 2500) {
            level = 5;
        } else if (score <= 3500) {
            level = 6;
        } else if (score <= 5000) {
            level = 7;
        } else {
            level = -1;
        }
        return level;
    }


    private static class RandomiseListAsyncTask extends AsyncTask<Void, Void, List<Friends>> {
        List<Friends> usersList = new ArrayList<>();
        long type;

        public RandomiseListAsyncTask(long type) {
            this.type = type;
        }

        @Override
        protected List<Friends> doInBackground(Void... voids) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("Users").whereEqualTo("type", type)
                    .orderBy("lastTimestamp").limit(5)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    if (!doc.getDocument().getId().equals(Objects.requireNonNull(userId))) {
                                        Friends friends = doc.getDocument().toObject(Friends.class).withId(Objects.requireNonNull(doc.getDocument().getString("id")));
                                        usersList.add(friends);
                                    }
                                }
                            }
                            try {
                                Collections.shuffle(usersList);
                            } catch (IndexOutOfBoundsException h) {

                            }
                        }
                    })
                    .addOnFailureListener(e -> {

                    });
            return usersList;
        }
    }
}