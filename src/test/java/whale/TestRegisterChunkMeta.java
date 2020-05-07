package whale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TestRegisterChunkMeta {

	String api = "https://apis.qianbao.com/basicservice/v1/intranet/filer";
//	String api = "http://10.19.40.112:9340/filer";
	String meta = "{\"chunks\":[{\"fid\":\"569,1b29e6728a5a5b31\",\"offset\":0,\"size\":12582912},{\"fid\":\"565,1b29e695fad1b011\",\"offset\":12582912,\"size\":12582912},{\"fid\":\"568,1b29e6a2ba6a5f79\",\"offset\":25165824,\"size\":12582912},{\"fid\":\"569,1b29e6ba4bf1b5ac\",\"offset\":37748736,\"size\":12582912},{\"fid\":\"566,1b29e6c2a7210003\",\"offset\":50331648,\"size\":12582912},{\"fid\":\"565,1b29e6c9358272a4\",\"offset\":62914560,\"size\":12582912},{\"fid\":\"565,1b29e6d0ba3fba02\",\"offset\":75497472,\"size\":12582912},{\"fid\":\"567,1b29e6e1c8bab050\",\"offset\":88080384,\"size\":9623456}],\"mime\":\"application/octet-stream\",\"name\":\"whale-0.0.0-SNAPSHOT.jar\",\"size\":97703840}";
	
//	String api = "https://dev-apis.qianbao.com/basicservice/v1/intranet/filer";
//	String meta = "{\"chunks\":[{\"fid\":\"20,020546a45eadbc41\",\"offset\":0,\"size\":12582912},{\"fid\":\"21,020546a53e66a00a\",\"offset\":12582912,\"size\":12582912},{\"fid\":\"17,020546a695881713\",\"offset\":25165824,\"size\":12582912},{\"fid\":\"21,020546a7cec1d621\",\"offset\":37748736,\"size\":12582912},{\"fid\":\"20,020546c478b667e1\",\"offset\":50331648,\"size\":12582912},{\"fid\":\"18,02054708b6339e38\",\"offset\":62914560,\"size\":12582912},{\"fid\":\"17,0205472fad986812\",\"offset\":75497472,\"size\":12582912},{\"fid\":\"20,020547338ae6abe4\",\"offset\":88080384,\"size\":9623456}],\"mime\":\"application/octet-stream\",\"name\":\"whale-0.0.0-SNAPSHOT.jar\",\"size\":97703840}";
	
	public static void main(String[] args){
		try{
			new TestRegisterChunkMeta().register();
		}catch(Throwable th){
			th.printStackTrace();
		}
	}
	
	private void register() throws Exception{
		Builder builder = new MultipartBody.Builder()
				.setType(MultipartBody.FORM);
		// 上传文件分割元数据
					builder.addFormDataPart(
							"meta",
							"/public/test/meta",
							RequestBody.create(MediaType
									.parse("application/json"),
									"{\"action\":\"registerChunkMeta\"}"
									.getBytes("UTF-8")));
					builder.addFormDataPart(
							"upload_chunks_json",
							"/public/test/w.jar",
							RequestBody.create(MediaType
									.parse("application/json"), meta));
					
		String jsonStr = upload(builder.build());
		System.out.println("response json: "+jsonStr);
	}
	
	private String upload(RequestBody reqBody)
			throws Exception{
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
        		.url(api)
        		.addHeader("Uni-Source", "KelpTesting")
                .post(reqBody).build();
        Response response = client.newCall(request).execute();
        String resp = null;
        if(response.body()!=null){
        	resp = new String(response.body().bytes(), "UTF-8");
        }
        if (!response.isSuccessful()) {
        	System.out.println("Upload error: "+response);
        	System.out.println(resp);
            return null;
        }
//        if(LOGGING){
//        	log.debug("response: "+respCode+"; "+resp);
//        }
        return resp;
	}
}
