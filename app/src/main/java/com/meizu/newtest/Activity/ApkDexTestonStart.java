package com.meizu.newtest.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.meizu.newtest.R;
import com.meizu.newtest.service.RunUiaService;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * Created by libinhui on 2016/5/12.
 */
public class ApkDexTestonStart extends AppCompatActivity{
    private static final String packagePath = "com.meizu.nltemtbf.test";
    private static final String classPath = "com.meizu.nltemtbf.ApplicationTest";
    private static String apkName = null;
    private int checkNum;
    private TextView tv_show, classnametv;
    private Button bt_selectall, bt_cancel, bt_deselectall, bt_runtest, bt_stoptest;
    private ListView lv;
    private MyAdapter madapter;
    private String classname = null;
    ArrayList<String> tcs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runtestmain);
        lv = (ListView) findViewById(R.id.lv);
        bt_selectall = (Button) findViewById(R.id.bt_selectall);
        bt_cancel = (Button) findViewById(R.id.bt_cancleselectall);
        bt_deselectall = (Button) findViewById(R.id.bt_deselectall);
        bt_runtest = (Button) findViewById(R.id.bt_runtest);
        bt_stoptest = (Button) findViewById(R.id.bt_stoptest);
        tv_show = (TextView) findViewById(R.id.tv);
        classnametv = (TextView) findViewById(R.id.className);

        try {
            apkName = getPackageManager().getApplicationInfo(packagePath, 0).sourceDir;
            tcs = getTestCase();
            Log.i("tcs", String.valueOf(tcs));
            madapter = new MyAdapter(tcs,this);
            lv.setAdapter(madapter);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();}
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 全选按钮的回调接口
        bt_selectall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 遍历list的长度，将MyAdapter中的map值全部设为true
                for (int i = 0; i < tcs.size(); i++) {
                    MyAdapter.getIsSelected().put(i, true);
                }
                // 数量设为list的长度
                checkNum = tcs.size();
                // 刷新listview和TextView的显示
                dataChanged();
            }
        });

        // 反选按钮的回调接口
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 遍历list的长度，将已选的设为未选，未选的设为已选
                for (int i = 0; i < tcs.size(); i++) {
                    if (MyAdapter.getIsSelected().get(i)) {
                        MyAdapter.getIsSelected().put(i, false);
                        checkNum--;
                    } else {
                        MyAdapter.getIsSelected().put(i, true);
                        checkNum++;
                    }
                }
                // 刷新listview和TextView的显示
                dataChanged();
            }
        });

        // 取消按钮的回调接口
        bt_deselectall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 遍历list的长度，将已选的按钮设为未选
                for (int i = 0; i < tcs.size(); i++) {
                    if (MyAdapter.getIsSelected().get(i)) {
                        MyAdapter.getIsSelected().put(i, false);
                        checkNum--;// 数量减1
                    }
                }
                // 刷新listview和TextView的显示
                dataChanged();
            }
        });

        // 绑定listView的监听器
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
                MyAdapter.ViewHolder holder = (MyAdapter.ViewHolder) arg1.getTag();
                // 改变CheckBox的状态
                holder.cb.toggle();
                // 将CheckBox的选中状况记录下来
                MyAdapter.getIsSelected().put(arg2, holder.cb.isChecked());
                // 调整选定条目
                if (holder.cb.isChecked() == true) {
                    checkNum++;
                } else {
                    checkNum--;
                }
                // 用TextView显示
                tv_show.setText("已选中" + checkNum + "项");
            }
        });

        bt_runtest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(ApkDexTestonStart.this, RunUiaService.class);
                ApkDexTestonStart.this.stopService(i);
                i.putExtra("testpkg", "com.meizu.nltemtbf");
                i.putExtra("testclass","ApplicationTest");
                i.putExtra("testcase", "test007Email");
//                ApkDexTest.this.stopService(i);
                ApkDexTestonStart.this.startService(i);
            }
        });

        bt_stoptest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(ApkDexTestonStart.this, RunUiaService.class);
                ApkDexTestonStart.this.stopService(i);
            }
        });


        try {
            classname = getApkClass();
            classnametv.setText("测试类名："+classname);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public String getApkClass() throws IOException {
        //获取测试的类名
        DexFile dx = DexFile.loadDex(apkName, File.createTempFile("opt", "dex", getCacheDir()).getPath(), 0);
        for (Enumeration<String> classNames = dx.entries(); classNames.hasMoreElements(); ) {
            String cname = classNames.nextElement();
            if (cname.contains("ApplicationTest")) {
                if (!cname.contains("$")) {
                    return cname;
                }
            }
        }
        return null;
    }

    public ArrayList<String> getTestCase() throws ClassNotFoundException {
        ArrayList<String> testcases = new ArrayList<String>();
        //获取方法
//        String[] = null;
        PathClassLoader pathClassLoader = new dalvik.system.PathClassLoader(
                apkName,
                ClassLoader.getSystemClassLoader());
        Class<?> handler = Class.forName(classPath, true, pathClassLoader);
        Method[] mts = handler.getMethods();
        for (Method mt : mts) {
            String testcasename = mt.getName();
            Boolean x = testcasename.startsWith("test");
            if(x){
                testcases.add(testcasename);
            }
        }
        return testcases;
    }

    // 刷新listview和TextView的显示
    private void dataChanged() {
        // 通知listView刷新
        madapter.notifyDataSetChanged();
        // TextView显示最新的选中数目
        tv_show.setText("已选中" + checkNum + "项");
    };
}
