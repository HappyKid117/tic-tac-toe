package androidsamples.java.tictactoe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {
  public List<Match> mEntries;
  private  List<Match> mOldEntries;
  private static final String TAG = "OpenGamesAdapter";
  private final LayoutInflater mInflater;
  private Context mContext;
  NavController mNavController;
  private SharedViewModel mSharedViewModel;
  private DatabaseReference ref;
  public OpenGamesAdapter(Context context, NavController navController, SharedViewModel sharedViewModel) {
    mEntries = new ArrayList<>();
    mContext = context;
    mNavController = navController;
    // FIXME if needed
    mInflater = LayoutInflater.from(context);
    mSharedViewModel = sharedViewModel;
    ref = FirebaseDatabase.getInstance().getReference();
    mContext = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
    // TODO bind the item at the given position to the holder
    if (mEntries != null) {
      Match current = mEntries.get(position);
      holder.mUsername.setText(current.player1);
      holder.mId.setText(Integer.toString(position+1));
      holder.matchId = current.id;
      holder.gameCode.setText(Integer.toString(Math.abs((current.id.hashCode() % 8999) + 1001)));
      ref.child("User").child(current.player1).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
          Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
          holder.mW.setText(map.get("wins").toString());
          holder.mL.setText(map.get("losses").toString());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
      });
    }
  }

  @Override
  public int getItemCount() {
    if(mEntries == null) return 0;
    return mEntries.size();
  }

  @SuppressLint("NotifyDataSetChanged")
  public void addEntry(Match match) {
    mEntries.add(match);
    notifyDataSetChanged();
  }

  @SuppressLint("NotifyDataSetChanged")
  public void clearEntries(){
    mEntries.clear();
    notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mUsername;
    public final TextView mW;
    public final TextView mL;
    public final TextView mId;
    public final TextView gameCode;
    public String matchId;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mUsername = (TextView) view.findViewById(R.id.txt_item_username);
      mW = (TextView) view.findViewById(R.id.txt_item_w);
      mL = (TextView) view.findViewById(R.id.txt_item_l);
      mId = (TextView) view.findViewById(R.id.item_number);
      gameCode = (TextView) view.findViewById(R.id.txt_item_code);
      view.setOnClickListener(this::launchGameFragment);
    }

    private void launchGameFragment(View v) {
      if(mUsername.getText().toString().equals(mSharedViewModel.username)){
        Toast.makeText(mContext, "You cannot join your own game", Toast.LENGTH_SHORT).show();
        return;
      }
      mSharedViewModel.new2PlayerGame = 0;
      mSharedViewModel.newGameLaunched = 1;
      NavDirections action = DashboardFragmentDirections.actionGame("Two-Player", matchId);
      mNavController.navigate(action);

    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " Game created by '" + mUsername.getText() + "'";
    }
  }
}