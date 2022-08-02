package com.linde.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.linde.bean.DrugBean;
import com.linde.refrigeratormanagementsystem.R;

import java.util.HashSet;
import java.util.List;

public class OutDrugAdapter extends RecyclerView.Adapter<OutDrugAdapter.ViewHolder> {
    private List<List<DrugBean>> allDrugBeanList;

    //构造函数,用于吧要展示的数据源传入，并赋予值给累的私有变量
    public OutDrugAdapter(List<List<DrugBean>> allDrugBeanList){
        this.allDrugBeanList=allDrugBeanList;
    }

    /**
     * onCreateViewHolder() 方法是用于创建ViewHolder 实例的，
     * 我们在这个方法中将item_Fruit 布局加载进来， 然后创建一个ViewHolder 实例，
     * 并把加载出来的布局传入到构造函数当中， 最后将ViewHolder 的实例返回。
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public OutDrugAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drug_out,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    /**
     * onBindViewHolder() 方法是用于对RecyclerView子项的数据进行赋值的，
     * 会在每个子项被滚动到屏幕内的时候执行， 这里我们通过position 参数得到当前项的Fruit实例，
     * 然后再将数据设置到ViewHolder 的ImageView和TextView当中即可
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull OutDrugAdapter.ViewHolder holder, int position) {
        List<DrugBean> drugBeanList=allDrugBeanList.get(position);
        holder.tvDrugName.setText(drugBeanList.get(0).getDrugName());
        holder.tvNO.setText(position+"");
        holder.tvDrugNumber.setText(drugBeanList.size()+"");
    }

    @Override
    public int getItemCount() {
        return allDrugBeanList.size();
    }

    /**
     * 定义内部类ViewHolder，并继承RecyclerView.ViewHolder
     * 传入View参数通常是Recycler View子项的最外层布局
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvNO;
        TextView tvDrugName;
        TextView tvDrugNumber;

        public ViewHolder(View itemView){
            super(itemView);
            tvNO=itemView.findViewById(R.id.tvNO);
            tvDrugName=itemView.findViewById(R.id.tvDrugName);
            tvDrugNumber=itemView.findViewById(R.id.tvDrugNumber);
        }
    }



}
