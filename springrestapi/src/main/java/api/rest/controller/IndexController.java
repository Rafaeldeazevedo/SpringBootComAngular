package api.rest.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import api.rest.exceptions.GeralException;
import api.rest.model.Usuario;
import api.rest.model.UsuarioDTO;
import api.rest.repository.UsuarioRepository;


@RestController  /* Arquitetura REST */
@RequestMapping(value = "/usuario")
public class IndexController {

    @Autowired /* de fosse CDI seria @Inject*/
    private UsuarioRepository usuarioRepository;


    /* Serviço RESTful */
    @GetMapping(value = "/{id}/codigovenda/{venda}", produces = "application/json")
    public ResponseEntity<Usuario> relatorio(@PathVariable(value = "id") Long id
            , @PathVariable(value = "venda") Long venda) {

        Optional<Usuario> usuario = usuarioRepository.findById(id);

        /*o retorno seria um relatorio*/
        return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
    }


    /* Serviço RESTful */
    
    @GetMapping(value = "/{id}", produces = "application/json")
    @Cacheable("cacheuser")
    @CacheEvict(value = "cacheuser", allEntries = true)
    @CachePut("cacheuser")
    public ResponseEntity<UsuarioDTO> init(@PathVariable(value = "id") Long id) {

        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return new ResponseEntity<UsuarioDTO>(new UsuarioDTO(usuario.get()), HttpStatus.OK);
    }

    
 
 
    
    @DeleteMapping(value = "/{id}", produces = "application/text")
    public String delete(@PathVariable("id") Long id) {

        usuarioRepository.deleteById(id);

        return "ok";
    }


    @DeleteMapping(value = "/{id}/venda", produces = "application/text")
    public String deletevenda(@PathVariable("id") Long id) {

        usuarioRepository.deleteById(id);

        return "ok";
    }


    /*Vamos supor que o carregamento de usuário seja um processo lento
	 * e queremos controlar ele com cache para agilizar o processo*/
    @GetMapping(value = "/", produces = "application/json")
    @CacheEvict(value = "cacheusuarios", allEntries = true)
    @CachePut("cacheusuarios")
    public ResponseEntity<List<Usuario>> usuario () throws InterruptedException {

        List<Usuario> list = (List<Usuario>) usuarioRepository.findAll();
        
		Thread.sleep(6000);/*Segura o codigo por 6 segunos simulando um processo lento*/


        return new ResponseEntity<List<Usuario>>(list, HttpStatus.OK);
    }


    @PostMapping(value = "/", produces = "application/json")
    public ResponseEntity<?> cadastrar(@RequestBody Usuario usuario) throws Exception {

        for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
            usuario.getTelefones().get(pos).setUsuario(usuario);
        }

        
        //** CONSUMINDO API PUBLICA EXTERNA
        
        URL url = new URL("https://viacep.com.br/ws/"+usuario.getCep()+"/json/");
        URLConnection connection = url.openConnection();
        InputStream is = connection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        
        String cep = "";
        StringBuilder jsonCep = new StringBuilder();
        
        while((cep = br.readLine()) != null)
        	jsonCep.append(cep);
        
        Usuario userAux = new Gson().fromJson(jsonCep.toString(), Usuario.class);
        
        usuario.setSenha(userAux.getCep());
        usuario.setLogradouro(userAux.getLogradouro());
        usuario.setComplemento(userAux.getComplemento());
        usuario.setBairro(userAux.getBairro());
        usuario.setLocalidade(userAux.getLocalidade());
        usuario.setUf(userAux.getUf());
        
        
        //** CONSUMINDO API PUBLICA EXTERNA

        
        
        
        usuario.setSenha(new BCryptPasswordEncoder().encode(usuario.getSenha()));

        Usuario usuarioBuscado = usuarioRepository.findUserByLogin(usuario.getLogin());
        if (usuarioBuscado != null) {
            throw new GeralException("Usuário já cadastrado");
        }
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        usuarioSalvo.setSenha("NÃO DISPONIVEL");
        return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);

    }


    @PutMapping(value = "/", produces = "application/json")
    public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario) {

        /*outras rotinas antes de atualizar*/

        for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
            usuario.getTelefones().get(pos).setUsuario(usuario);
        }

        
        
        
        
        
        Usuario userTemporario = usuarioRepository.findUserByLogin(usuario.getLogin());
        
        if(! userTemporario.getSenha().equals(usuario.getSenha())) {     /*SENHAS DIFERENTES*/
        	
        	 usuario.setSenha(new BCryptPasswordEncoder().encode(usuario.getSenha()));
        	
        }
        
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);

    }


    @PutMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
    public ResponseEntity updateVenda(@PathVariable Long iduser,
                                      @PathVariable Long idvenda) {
        /*outras rotinas antes de atualizar*/

        //Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return new ResponseEntity("Venda atualzada", HttpStatus.OK);

    }


    @PostMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
    public ResponseEntity cadastrarvenda(@PathVariable Long iduser,
                                         @PathVariable Long idvenda) {

        /*Aqui seria o processo de venda*/
        //Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return new ResponseEntity("id user :" + iduser + " idvenda :" + idvenda, HttpStatus.OK);

    }


}