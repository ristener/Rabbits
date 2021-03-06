package com.kyleduo.rabbits.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kyleduo.rabbits.P;
import com.kyleduo.rabbits.Rabbit;
import com.kyleduo.rabbits.RabbitResult;
import com.kyleduo.rabbits.annotations.Page;
import com.kyleduo.rabbits.demo.base.BaseActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Page("/")
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        setContentView(view);

        List<Section> data = new ArrayList<>();

        data.add(new Section(
                "Standard Usages",
                "/test",
                "/test?param=value",
                "/test/value",
                "demo://rabbits.kyleduo.com/test/value",
                "/test_variety",
                "xxx://xxx.xxx/xxx?param=xxx"
        ));
        data.add(new Section("startForResult", "Result: " + P.P_SECOND_ID(1)));
        data.add(new Section("Interceptors", "/test/interceptor", "/test/rules"));
        data.add(new Section("Fallback", "https://kyleduo.com"));
        data.add(new Section("Control", "Redirect: /test/redirect", "Anim: /test/animation", "IgnoreInterceptor: /test/interceptor?not_intercepted", "IgnoreFallback: https://kyleduo.com"));
        data.add(new Section("Fragment", "/test_fragment", "/web"));
        data.add(new Section("Dump route table", "/dump"));
        data.add(new Section("Multiple modules", "/sm1/activity", "/sm2/activity"));
        data.add(new Section("copyright @kyleduo 2018"));
        data.add(new Section(""));

        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(new TestAdapter(this, data));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Toast.makeText(this, "Result: " + data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Result: " + resultCode, Toast.LENGTH_SHORT).show();
            }
        }
    }

    static class Section {
        String name;
        List<Item> items;

        Section(String name, String... items) {
            this.name = name;
            this.items = new ArrayList<>();
            if (items != null) {
                for (String item : items) {
                    this.items.add(new Item(item));
                }
            }
        }
    }

    static class Item {
        String name;

        Item(String name) {
            this.name = name;
        }
    }

    static class IndexPath {
        int section;
        int index;

        IndexPath(int section, int index) {
            this.section = section;
            this.index = index;
        }

        static IndexPath create(int section, int index) {
            return new IndexPath(section, index);
        }
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {

        TextView titleTv;

        SectionViewHolder(View itemView) {
            super(itemView);
            titleTv = (TextView) itemView.findViewById(R.id.section_title);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView titleTv;

        ItemViewHolder(View itemView) {
            super(itemView);
            titleTv = (TextView) itemView.findViewById(R.id.item_title);
        }
    }

    static class TestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<Section> mData;
        List<IndexPath> mIndexPaths;
        private WeakReference<MainActivity> mActRef;

        TestAdapter(MainActivity activity, List<Section> data) {
            mActRef = new WeakReference<>(activity);
            mData = data;
            mIndexPaths = new ArrayList<>();
            for (int i = 0; i < mData.size(); i++) {
                mIndexPaths.add(IndexPath.create(i, -1));
                Section s = mData.get(i);
                for (int j = 0; j < s.items.size(); j++) {
                    mIndexPaths.add(IndexPath.create(i, j));
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View view = LayoutInflater.from(DemoApplication.get()).inflate(R.layout.item_section_header, parent, false);
                return new SectionViewHolder(view);
            } else if (viewType == 1) {
                View view = LayoutInflater.from(DemoApplication.get()).inflate(R.layout.item, parent, false);
                final ItemViewHolder holder = new ItemViewHolder(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        IndexPath indexPath = mIndexPaths.get(position);
                        if (indexPath.index >= 0) {
                            Item item = mData.get(indexPath.section).items.get(indexPath.index);
                            String url = item.name;
                            RabbitResult result;
                            if (url.startsWith("Redirect: ")) {
                                url = url.substring(10);
                                result = Rabbit.from(mActRef.get()).to(url).redirect().start();
                            } else if (url.startsWith("Anim: ")) {
                                url = url.substring(6);
                                result = Rabbit.from(mActRef.get()).to(url).setTransitionAnimations(new int[]{R.anim.fadein, R.anim.fadeout}).start();
                            } else if (url.startsWith("IgnoreInterceptor: ")) {
                                url = url.substring(19);
                                result = Rabbit.from(mActRef.get()).to(url).ignoreInterceptors().start();
                            } else if (url.startsWith("IgnoreFallback: ")) {
                                url = url.substring(16);
                                result = Rabbit.from(mActRef.get()).to(url).ignoreFallback().start();
                            } else if (url.startsWith("Result: ")) {
                                url = url.substring(8);
                                result = Rabbit.from(mActRef.get()).to(url).startForResult(100);
                            } else {
                                result = Rabbit.from(mActRef.get()).to(url).start();
                            }
                            if (result.isFinished() && !result.isSuccess()) {
                                Toast.makeText(mActRef.get(), "Navigation Fail", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                return holder;
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int type = getItemViewType(position);
            IndexPath indexPath = mIndexPaths.get(position);
            if (type == 0) {
                ((SectionViewHolder) holder).titleTv.setText(mData.get(indexPath.section).name);
            } else if (type == 1) {
                ((ItemViewHolder) holder).titleTv.setText(mData.get(indexPath.section).items.get(indexPath.index).name);
            }
        }

        @Override
        public int getItemCount() {
            return mIndexPaths.size();
        }

        @Override
        public int getItemViewType(int position) {
            IndexPath indexPath = mIndexPaths.get(position);
            return indexPath.index == -1 ? 0 : 1;
        }
    }
}
