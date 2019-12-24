package fck.friend.rent.integration;

import fck.friend.rent.dto.post.PostBunchDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class PostsProvider {
    private final WebClient vkWebClient;

    private final String postsUrl;

    public PostsProvider(@Qualifier("vkWebClient")
                                 WebClient vkWebClient,
                         @Value("${spring.security.oauth2.client.registration.vk-login-client.api.posts}")
                                 String postsUrl) {
        this.vkWebClient = vkWebClient;
        this.postsUrl = postsUrl;
    }

    public Mono<PostBunchDto> get(OAuth2AuthorizedClient authorizedClient) {

//        List<String> users = Arrays.asList("2168853", "4138136", "4787534", "5956315", "6374616", "6864202", "7628049", "7920061", "9673159", "11537673", "12682323", "14170210", "15645171", "17171072", "17219005", "17736856", "18731068", "20388364", "20673461", "20936501", "21260339", "21273938", "21305151", "22882883", "23037982", "27442480", "27450907", "27492146", "27837718", "28293737", "29004525", "29127448", "29238316", "31319720", "31545722", "32581488", "32979927", "34893902", "36818498", "37883407", "38479163", "38683144", "40031687", "41493804", "42444409", "43277027", "44108418", "45479264", "48198442", "48370274", "49586757", "49601379", "49787369", "50028543", "53870389", "63375170", "67579519", "78260314", "80141920", "82655942", "86187869", "92549950", "94297713", "105250021", "109419366", "113988948", "120777746", "130165858", "132847058", "134419961", "134650982", "142177133", "142455228", "142868290", "143809312", "145324551", "150324782", "154606931", "164933717", "166387159", "171717297", "174395381", "176817121", "177628457", "182504830", "186208117", "207107219", "212569068", "214767695", "222964814", "232538626", "250145454", "257711324", "284033708", "312356499", "360843730", "424610342", "437006887", "444508802", "460007679", "520671707");
        List<String> users = Arrays.asList("2168853", "4138136", "4787534", "5956315");

        final int chunkSize = 24;
        final AtomicInteger counter = new AtomicInteger();
        final Collection<List<String>> result = users.stream()
                                                     .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
                                                     .values();
        List<String> chunkedUsers = result.stream()
                                          .map(list -> String.join(",", list))
                                          .collect(Collectors.toList());


        return vkWebClient.get()
                          .uri(postsUrl + "?users=" + String.join(",", chunkedUsers)+"&access_token="+authorizedClient.getAccessToken().getTokenValue()+"&v=5.03")
                          .retrieve()
                          .bodyToMono(PostBunchDto.class);
        /*Flux.<PostBunchDto>create(fluxSink ->
                chunkedUsers.stream()
                            .map(chunkedUser -> vkRestTemplate.getForEntity(postsUrl + "?users=" + chunkedUser, PostBunchDto.class))
                            .map(HttpEntity::getBody)
                            .forEach(fluxSink::next))
                .log()
                .sample(Duration.ofSeconds(1));*/
//        WebClient.create().mutate().exchangeFunction()
    }
}
