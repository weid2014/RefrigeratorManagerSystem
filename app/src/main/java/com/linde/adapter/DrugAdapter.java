package com.linde.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.linde.bean.DrugBean;
import com.linde.refrigeratormanagementsystem.R;

import java.util.List;

public class DrugAdapter extends RecyclerView.Adapter<DrugAdapter.ViewHolder> {
    private List<DrugBean> drugBeanList;

    //构造函数,用于吧要展示的数据源传入，并赋予值给累的私有变量
    public DrugAdapter(List<DrugBean> drugBeanList){
        this.drugBeanList=drugBeanList;
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
    public DrugAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drug,parent,false);
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
    public void onBindViewHolder(@NonNull DrugAdapter.ViewHolder holder, int position) {
        DrugBean drugBean=drugBeanList.get(position);
        holder.tvDrugName.setText(drugBean.getDrugName());
        holder.tvDrugNo.setText(drugBean.getDrugNo());
        holder.tvWareHousingTime.setText(drugBean.getWareHousingTime());
        holder.tvDrugSN.setText(drugBean.getDrugSN());
    }

    @Override
    public int getItemCount() {
        return drugBeanList.size();
    }

    /**
     * 定义内部类ViewHolder，并继承RecyclerView.ViewHolder
     * 传入View参数通常是Recycler View子项的最外层布局
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvDrugName;
        TextView tvDrugNo;
        TextView tvWareHousingTime;
        TextView tvDrugSN;

        public ViewHolder(View itemView){
            super(itemView);
            tvDrugName=itemView.findViewById(R.id.tvDrugName);
            tvDrugNo=itemView.findViewById(R.id.tvDrugNo);
            tvWareHousingTime=itemView.findViewById(R.id.tvWareHousingTime);
            tvDrugSN=itemView.findViewById(R.id.tvDrugSN);
        }
    }


}
